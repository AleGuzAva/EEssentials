package EEssentials.commands.other;

import EEssentials.commands.AliasedCommand;
import EEssentials.lang.LangManager;
import EEssentials.util.ColorUtil;
import EEssentials.util.IgnoreManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;

/**
 * Provides command to send a private message.
 */
public class MessageCommands {

    // Permission nodes for the commands
    private static final String MESSAGE_PERMISSION_NODE = "eessentials.msg";

    private static final Map<ServerPlayerEntity, ServerPlayerEntity> lastMessageSenders = new HashMap<>();

    public static void storeLastSender(ServerPlayerEntity recipient, ServerPlayerEntity sender) {
        lastMessageSenders.put(recipient, sender);
    }

    /**
     * Registers the message command.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        // Unregister the existing message commands
        dispatcher.getRoot().getChildren().removeIf(literalCommandNode -> "message".equals(literalCommandNode.getName()));
        dispatcher.getRoot().getChildren().removeIf(literalCommandNode -> "msg".equals(literalCommandNode.getName()));
        dispatcher.getRoot().getChildren().removeIf(literalCommandNode -> "tell".equals(literalCommandNode.getName()));
        dispatcher.getRoot().getChildren().removeIf(literalCommandNode -> "whisper".equals(literalCommandNode.getName()));
        dispatcher.getRoot().getChildren().removeIf(literalCommandNode -> "w".equals(literalCommandNode.getName()));

        // Register /message, /msg, /tell, /whisper, /w
        new AliasedCommand() {
            @Override
            public LiteralCommandNode<ServerCommandSource> register(CommandDispatcher<ServerCommandSource> dispatcher) {
                return dispatcher.register(CommandManager.literal("message")
                        .requires(Permissions.require(MESSAGE_PERMISSION_NODE, 2))
                        .then(argument("target", EntityArgumentType.player())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(ctx.getSource().getServer().getPlayerNames(), builder))
                                .then(argument("message", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                                            String message = StringArgumentType.getString(ctx, "message");
                                            return sendMessage(ctx, target, message);  // Sends the private message
                                        }))));
            }

            @Override
            public String[] getCommandAliases() {
                return new String[]{"msg", "tell", "whisper", "w"};
            }
        }.registerWithAliases(dispatcher);

        // Register /reply, /r
        new AliasedCommand() {
            @Override
            public LiteralCommandNode<ServerCommandSource> register(CommandDispatcher<ServerCommandSource> dispatcher) {
                return dispatcher.register(CommandManager.literal("reply")
                        .requires(Permissions.require(MESSAGE_PERMISSION_NODE, 2))
                        .then(CommandManager.argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String message = StringArgumentType.getString(ctx, "message");
                                    return sendReply(ctx, message);  // Handle the reply
                                })));
            }

            @Override
            public String[] getCommandAliases() {
                return new String[]{"r"};
            }
        }.registerWithAliases(dispatcher);
    }


    /**
     * Sends Social Spy messages to the Spy.
     *
     * @param ctx The command context.
     * @param message The message to be sent.
     */
    private static void sendToSocialSpies(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity sender, ServerPlayerEntity receiver, String message) {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("{sender}", sender.getName().getString());
        replacements.put("{receiver}", receiver.getName().getString());
        replacements.put("{message}", message);

        // Fetch and format the Social Spy message using LangManager
        String socialSpyMessage = LangManager.getLang("Prefix-Social-Spy");
        if (socialSpyMessage != null) {
            // Replace placeholders in the Social Spy message
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                socialSpyMessage = socialSpyMessage.replace(entry.getKey(), entry.getValue());
            }

            // Use ColorUtil to parse the formatted message into a Component
            Component componentMessage = ColorUtil.parseColour(socialSpyMessage);

            for (ServerPlayerEntity spy : ctx.getSource().getServer().getPlayerManager().getPlayerList()) {
                if (SocialSpyCommand.isSocialSpyEnabled(spy)) {
                    // Use Adventure's Audience to send the message
                    spy.sendMessage(componentMessage);
                }
            }
        }
    }


    /**
     * Sends a private message to the target player.
     *
     * @param ctx The command context.
     * @param target The target player.
     * @param message The message to be sent.
     * @return 1 if successful, 0 otherwise.
     */

    private static int sendMessage(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity target, String message) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null || target == null) return 0;

        Map<String, String> replacements = new HashMap<>();
        replacements.put("{receiver}", target.getName().getString());
        replacements.put("{sender}", player.getName().getString());
        replacements.put("{message}", message);

        // Check if the target has ignored the sender
        if (IgnoreManager.hasIgnored(target, player)) {
            LangManager.send(player, "Ignore", replacements); // Update this line with the correct lang key
            return 1;
        }

        LangManager.send(target, "Message-Receive", replacements);
        LangManager.send(player, "Message-Send", replacements);

        // Social Spy
        sendToSocialSpies(ctx, player, target, message);

        // Store this interaction so that the target can reply back
        storeLastSender(target, player);

        return 1;
    }

    private static int sendReply(CommandContext<ServerCommandSource> ctx, String message) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();

        ServerPlayerEntity target = lastMessageSenders.get(player);

        // Check if the target has ignored the sender
        if (IgnoreManager.hasIgnored(target, player)) {
            LangManager.send(player, "Ignore", Map.of("player", target.getName().getString()));
            return 1;
        }

        Map<String, String> replacements = new HashMap<>();
        replacements.put("{receiver}", target.getName().getString());
        replacements.put("{sender}", player.getName().getString());
        replacements.put("{message}", message);

        // Check if the target player is still online
        if (!isPlayerOnline(ctx.getSource().getServer(), target)) {
            LangManager.send(player, "No-Reply-Target");
            return 0;
        }

        LangManager.send(player, "Message-Send", replacements);
        LangManager.send(target, "Message-Receive", replacements);

        // Social Spy
        sendToSocialSpies(ctx, player, target, message);

        // Update the last sender for potential back-and-forth replies
        storeLastSender(target, player);

        return 1;
    }

    private static boolean isPlayerOnline(MinecraftServer server, ServerPlayerEntity player) {
        return server.getPlayerManager().getPlayer(player.getUuid()) != null;
    }
}
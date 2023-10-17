package EEssentials.commands.other;

import EEssentials.commands.AliasedCommand;
import EEssentials.util.IgnoreManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import static net.minecraft.server.command.CommandManager.*;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides command to send a private message.
 */
public class MessageCommands {

    /*
     * DO NOT GIVE THIS COMMAND ANY PERMISSION NODES AS /MSG IS OVERRWRITES THE VANILLA /MSG and /TELL
     * MEANING PLAYERS WON'T BE ABLE TO /MSG OFF THE RIP
     * Open to Discussing this btw ^_^ - Novoro
     */
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
    private static void sendToSocialSpies(CommandContext<ServerCommandSource> ctx, Text message) {
        for (ServerPlayerEntity spy : ctx.getSource().getServer().getPlayerManager().getPlayerList()) {
            if (SocialSpyCommand.isSocialSpyEnabled(spy)) {
                spy.sendMessage(message, false);
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

        Text senderMessage = Text.of("[me -> " + target.getName().getString() + "] " + message);
        Text targetMessage = Text.of("[" + player.getName().getString() + " -> me] " + message);

        // Check if the target has ignored the sender
        if (IgnoreManager.hasIgnored(target, player)) {
            player.sendMessage(Text.of(target.getName().getString() + " has you on their ignore list. You cannot send them a message"), false);
            return 1;
        }

        target.sendMessage(targetMessage, false);
        player.sendMessage(senderMessage, false);

        // Social Spy
        Text socialSpyMessage = Text.of("[" + player.getName().getString() + " -> " + target.getName().getString() + "] " + message);
        sendToSocialSpies(ctx, socialSpyMessage);

        // Store this interaction so that the target can reply back
        storeLastSender(target, player);

        return 1;

    }

    private static int sendReply(CommandContext<ServerCommandSource> ctx, String message) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (!lastMessageSenders.containsKey(player)) {
            player.sendMessage(Text.literal("You don't have anyone to reply to."), false);
            return 0;
        }

        ServerPlayerEntity target = lastMessageSenders.get(player);

        // Check if the target has ignored the sender
        if (IgnoreManager.hasIgnored(target, player)) {
            player.sendMessage(Text.of(target.getName().getString() + " has you on their ignore list. You cannot send them a message"), false);
            return 1;
        }

        Text senderMessage = Text.of("[me -> " + target.getName().getString() + "] " + message);
        Text targetMessage = Text.of("[" + player.getName().getString() + " -> me] " + message);


        target.sendMessage(targetMessage, false);
        player.sendMessage(senderMessage, false);

        // Social Spy
        Text socialSpyMessage = Text.of("[" + player.getName().getString() + " -> " + target.getName().getString() + "] " + message);
        sendToSocialSpies(ctx, socialSpyMessage);

        // Update the last sender for potential back-and-forth replies
        storeLastSender(target, player);

        return 1;
    }

}
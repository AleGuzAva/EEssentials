package EEssentials.commands.utility;

import EEssentials.lang.LangManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.argument.EntityArgumentType.player;

public class VanishCommand {

    public static final String VANISH_SELF_PERMISSION_NODE = "eessentials.vanish.self";
    public static final String VANISH_OTHER_PERMISSION_NODE = "eessentials.vanish.other";
    private static final Set<UUID> vanishedPlayers = new HashSet<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("vanish")
                        .requires(Permissions.require(VANISH_SELF_PERMISSION_NODE, 2))
                        .executes(ctx -> toggleVanish(ctx.getSource().getPlayer(), ctx))
                        .then(argument("target", player())
                                .requires(Permissions.require(VANISH_OTHER_PERMISSION_NODE, 2))
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(ctx.getSource().getServer().getPlayerNames(), builder))
                                .executes(ctx -> {
                                    ServerPlayerEntity target = getPlayer(ctx, "target");
                                    return toggleVanish(target, ctx);
                                }))
        );
    }

    private static int toggleVanish(ServerPlayerEntity player, CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        if (player == null) return 0;

        UUID playerId = player.getUuid();
        boolean isVanished = vanishedPlayers.contains(playerId);

        if (isVanished) {
            vanishedPlayers.remove(playerId);
            player.setInvisible(false);
            player.sendMessage(Text.of("You are now visible."), false);

            LangManager.send(player, "Vanish-Disabled");

            if (!player.equals(source.getPlayer())) {
                LangManager.send(source.getPlayer(), "Vanish-Other-Disabled", Map.of("{player}", player.getName().getString()));
            }

            // Update the player list to mark the player as listed
            ctx.getSource().getServer().getPlayerManager().getPlayerList().forEach(otherPlayer -> {
                otherPlayer.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, player));
            });

            // Send entity status packet to all players
            ctx.getSource().getServer().getPlayerManager().sendToAll(new EntityStatusS2CPacket(player, (byte) 35));
        } else {
            vanishedPlayers.add(playerId);
            player.setInvisible(true);
            player.sendMessage(Text.of("You have vanished."), false);

            LangManager.send(player, "Vanish-Enabled");

            if (!player.equals(source.getPlayer())) {
                LangManager.send(source.getPlayer(), "Vanish-Other-Enabled", Map.of("{player}", player.getName().getString()));
            }


            // Send entity status packet to all players
            ctx.getSource().getServer().getPlayerManager().sendToAll(new EntityStatusS2CPacket(player, (byte) 32));
        }

        return 1;
    }
}

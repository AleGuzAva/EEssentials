package EEssentials.commands.utility;

import EEssentials.lang.LangManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Provides command to toggle flight mode for players.
 */
public class FlyCommand {

    // Permission node for the fly command.
    public static final String FLY_SELF_PERMISSION_NODE = "eessentials.fly.self";
    public static final String FLY_OTHER_PERMISSION_NODE = "eessentials.fly.other";


    /**
     * Registers the fly command.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("fly")
                        .requires(Permissions.require(FLY_SELF_PERMISSION_NODE, 2))
                        .executes(ctx -> toggleFlight(ctx))  // Toggle flight for the executing player
                        .then(argument("target", EntityArgumentType.player())
                                .requires(Permissions.require(FLY_OTHER_PERMISSION_NODE, 2))
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(ctx.getSource().getServer().getPlayerNames(), builder))
                                .executes(ctx -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                                    return toggleFlight(ctx, target);  // Toggle flight for the specified player
                                }))
        );
    }

    /**
     * Toggles the flight mode for target players.
     *
     * @param ctx The command context.
     * @param targets The target players.
     * @return 1 if successful, 0 otherwise.
     */
    private static int toggleFlight(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity... targets) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = targets.length > 0 ? targets[0] : source.getPlayer();

        if (player == null) return 0;

        // Toggle the ability to fly
        boolean newFlightAbilityStatus = !player.getAbilities().allowFlying;
        player.getAbilities().allowFlying = newFlightAbilityStatus;

        // If the ability to fly is turned off, ensure the player is not currently flying
        if (!newFlightAbilityStatus) {
            player.getAbilities().flying = false;
        }

        player.sendAbilitiesUpdate();

        if (player.equals(source.getPlayer())) {
            String key = newFlightAbilityStatus ? "Flight-Enabled-Self" : "Flight-Disabled-Self";
            LangManager.send(player, key);
        } else {
            String key = newFlightAbilityStatus ? "Flight-Enabled-Other-Notify" : "Flight-Disabled-Other-Notify";
            LangManager.send(player, key, Map.of("{player}", player.getName().getString()));
            String notifyKey = newFlightAbilityStatus ? "Flight-Enabled-Other" : "Flight-Disabled-Other";
            LangManager.send(source, notifyKey, Map.of("{source}", source.getName()));
        }

        return 1;
    }

}

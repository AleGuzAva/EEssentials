package EEssentials.commands.utility;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import static net.minecraft.server.command.CommandManager.*;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Provides command to toggle flight mode for players.
 */
public class FlyCommand {

    // Permission node for the fly command.
    public static final String FLY_PERMISSION_NODE = "eessentials.fly";

    /**
     * Registers the fly command.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("fly")
                        .requires(Permissions.require(FLY_PERMISSION_NODE, 2))
                        .executes(ctx -> toggleFlight(ctx))  // Toggle flight for the executing player
                        .then(argument("target", EntityArgumentType.player())
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
            player.sendMessage(Text.of(newFlightAbilityStatus ? "Flight ability enabled." : "Flight ability disabled."), false);
        } else {
            player.sendMessage(Text.of(newFlightAbilityStatus ? "Flight ability enabled by " + source.getName() : "Flight ability disabled by " + source.getName()), false);
            source.sendMessage(Text.of(newFlightAbilityStatus ? "Enabled flight ability for " + player.getName().getString() + "." : "Disabled flight ability for " + player.getName().getString() + "."));
        }

        return 1;
    }

}

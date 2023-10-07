package EEssentials.commands.utility;

import EEssentials.mixins.PlayerAbilitiesMixin;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Defines the /speed command to adjust a player's flight speed.
 */
public class SpeedCommand {

    // Permission node required to adjust fly speed.
    public static final String FLY_SPEED_PERMISSION_NODE = "novoroessentials.user.speed.fly";

    /**
     * Registers the /speed fly <speedMultiplier> command.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("speed")
                        .requires(Permissions.require(FLY_SPEED_PERMISSION_NODE, 2))
                        .then(literal("fly")
                                .then(argument("speedMultiplier", FloatArgumentType.floatArg(0.1f, 5.0f))
                                        .executes(ctx -> executeSetFlightSpeed(ctx, FloatArgumentType.getFloat(ctx, "speedMultiplier")))
                                )
                        )
        );
    }

    /**
     * Executes the command to set the target player's flight speed.
     *
     * @param ctx            The command context.
     * @param speedMultiplier The multiplier for the flight speed (typically between 0.1 and 5).
     * @return Returns 1 to indicate the command executed successfully.
     */
    private static int executeSetFlightSpeed(CommandContext<ServerCommandSource> ctx, float speedMultiplier) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity target = source.getPlayer();

        if (target == null) {
            source.sendError(Text.of("This command cannot be run from the console without specifying a player."));
            return 1;
        }

        // Adjusts the flight speed of the player using the PlayerAbilitiesMixin.
        ((PlayerAbilitiesMixin) target.getAbilities()).setFlySpeed(0.05f * speedMultiplier);
        target.sendAbilitiesUpdate();
        ctx.getSource().sendFeedback(() -> Text.of("Set flight speed of " + target.getEntityName() + " to " + speedMultiplier + "x"), false);

        return 1;
    }

}

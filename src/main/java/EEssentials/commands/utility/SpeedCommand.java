package EEssentials.commands.utility;

import EEssentials.lang.LangManager;
import EEssentials.mixins.PlayerAbilitiesMixin;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Defines the /speed command to adjust a player's flight speed.
 */
public class SpeedCommand {

    // Permission node required to adjust speeds.

    public static final String SPEED_PERMISSION_NODE = "eessentials.speed.set";
    public static final String FLY_SPEED_PERMISSION_NODE = "eessentials.speed.fly";
    public static final String WALK_SPEED_PERMISSION_NODE = "eessentials.speed.walk";
    public static final String SPEED_OTHER_PERMISSION_NODE = "eessentials.speed.other";


    /**
     * Registers the "speed" command, which includes subcommands for both flying and walking speeds.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Base command
        LiteralArgumentBuilder<ServerCommandSource> speedCommand = literal("speed")
                .requires(src -> Permissions.check(src, SPEED_PERMISSION_NODE, 2));

        // Subcommand for flying speed
        RequiredArgumentBuilder<ServerCommandSource, Float> speedArgument = argument("speedMultiplier", FloatArgumentType.floatArg(0.1f, 10.0f))
                .requires(src -> Permissions.check(src, FLY_SPEED_PERMISSION_NODE, 2))
                .executes(ctx -> executeSetFlightSpeed(ctx, FloatArgumentType.getFloat(ctx, "speedMultiplier")));

        // Add support for an optional target player argument for the fly speed.
        speedArgument.then(argument("target", EntityArgumentType.player())
                .requires(src -> Permissions.check(src, SPEED_OTHER_PERMISSION_NODE, 2))
                .executes(ctx -> executeSetFlightSpeed(ctx, FloatArgumentType.getFloat(ctx, "speedMultiplier"))));  // The method will handle finding the target.

        speedCommand.then(literal("fly").then(speedArgument));

        // Repeat the same structure for walking speed.
        RequiredArgumentBuilder<ServerCommandSource, Float> walkSpeedArgument = argument("speedMultiplier", FloatArgumentType.floatArg(0.1f, 10.0f))
                .requires(src -> Permissions.check(src, WALK_SPEED_PERMISSION_NODE, 2))
                .executes(ctx -> executeSetWalkSpeed(ctx, FloatArgumentType.getFloat(ctx, "speedMultiplier")));

        walkSpeedArgument.then(argument("target", EntityArgumentType.player())
                .requires(src -> Permissions.check(src, SPEED_OTHER_PERMISSION_NODE, 2))
                .executes(ctx -> executeSetWalkSpeed(ctx, FloatArgumentType.getFloat(ctx, "speedMultiplier"))));  // The method will handle finding the target.

        speedCommand.then(literal("walk").then(walkSpeedArgument));

        // Register the constructed command
        dispatcher.register(speedCommand);
    }



    /**
     * Executes the command to set the target player's flight speed.
     *
     * @param ctx The command context.
     * @param speedMultiplier The multiplier for the flight speed (typically between 0.1 and 5).
     * @return Returns 1 to indicate the command executed successfully.
     */
    private static int executeSetFlightSpeed(CommandContext<ServerCommandSource> ctx, float speedMultiplier) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();

        // Adjust the flight speed
        ((PlayerAbilitiesMixin) player.getAbilities()).setFlySpeed(0.05f * speedMultiplier);
        player.sendAbilitiesUpdate();

        // Send appropriate messages
        Map<String, String> replacements = Map.of(
                "{speed-option}", "flight",
                "{speed-multiplier}", String.valueOf(speedMultiplier),
                "{player}", player.getName().getString()
        );
        LangManager.send(source, "Speed-Set-Self", replacements);

        return 1;
    }

    /**
     * Executes the command to set the target player's walking speed.
     *
     * @param ctx The command context.
     * @param speedMultiplier The multiplier for the walking speed (typically between 0.1 and 5).
     * @return Returns 1 to indicate the command executed successfully.
     */
    private static int executeSetWalkSpeed(CommandContext<ServerCommandSource> ctx, float speedMultiplier) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();

        // Adjust the walking speed
        ((PlayerAbilitiesMixin) player.getAbilities()).setWalkSpeed(0.1f * speedMultiplier);
        player.sendAbilitiesUpdate();

        // Send appropriate messages
        Map<String, String> replacements = Map.of(
                "{speed-option}", "walking",
                "{speed-multiplier}", String.valueOf(speedMultiplier),
                "{player}", player.getName().getString()
        );
        LangManager.send(source, "Speed-Set-Self", replacements);

        return 1;
    }



}

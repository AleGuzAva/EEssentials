package EEssentials.commands;

import EEssentials.util.PermissionHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Provides commands for quick game mode switching using aliases.
 */
public class GamemodeAliasesCommands {

    // Permission nodes for each game mode command.
    public static final String ADVENTURE_PERMISSION_NODE = "novoroessentials.user.gma";
    public static final String CREATIVE_PERMISSION_NODE = "novoroessentials.user.gmc";
    public static final String SPECTATOR_PERMISSION_NODE = "novoroessentials.user.gmsp";
    public static final String SURVIVAL_PERMISSION_NODE = "novoroessentials.user.gms";

    /**
     * Registers game mode switching commands.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Registers the /gmc command for changing to Creative mode.
        dispatcher.register(
                literal("gmc")
                        .requires(source -> hasPermission(source, CREATIVE_PERMISSION_NODE))
                        .executes(ctx -> executeGameMode(ctx, GameMode.CREATIVE))
        );

        // Registers the /gms command for changing to Survival mode.
        dispatcher.register(
                literal("gms")
                        .requires(source -> hasPermission(source, SURVIVAL_PERMISSION_NODE))
                        .executes(ctx -> executeGameMode(ctx, GameMode.SURVIVAL))
        );

        // Registers the /gmsp command for changing to Spectator mode.
        dispatcher.register(
                literal("gmsp")
                        .requires(source -> hasPermission(source, SPECTATOR_PERMISSION_NODE))
                        .executes(ctx -> executeGameMode(ctx, GameMode.SPECTATOR))
        );

        // Registers the /gma command for changing to Adventure mode.
        dispatcher.register(
                literal("gma")
                        .requires(source -> hasPermission(source, ADVENTURE_PERMISSION_NODE))
                        .executes(ctx -> executeGameMode(ctx, GameMode.ADVENTURE))
        );
    }

    /**
     * Changes the game mode of the player issuing the command.
     *
     * @param ctx      The command context.
     * @param gameMode The target game mode.
     * @return 1 if successful, 0 otherwise.
     */
    private static int executeGameMode(CommandContext<ServerCommandSource> ctx, GameMode gameMode) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        // Change the player's game mode and send a message to confirm.
        player.changeGameMode(gameMode);
        player.sendMessage(Text.literal("Set own game mode to " + gameMode + " Mode").formatted(Formatting.WHITE), false);
        return 1;
    }

    /**
     * Checks if a player has the required permissions to execute a command.
     *
     * @param source        The command source.
     * @param permissionNode The permission node for the command.
     * @return True if the player has permissions, false otherwise.
     */
    private static boolean hasPermission(ServerCommandSource source, String permissionNode) {
        return source.hasPermissionLevel(2) || PermissionHelper.hasPermission(source.getPlayer(), permissionNode);
    }
}

package EEssentials.commands;

import EEssentials.util.PermissionHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import static net.minecraft.server.command.CommandManager.*;
import net.minecraft.command.argument.EntityArgumentType;
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
    public static final String ADVENTURE_PERMISSION_NODE = "eessentials.gamemode.adventure";
    public static final String CREATIVE_PERMISSION_NODE = "eessentials.gamemode.creative";
    public static final String SPECTATOR_PERMISSION_NODE = "eessentials.gamemode.spectator";
    public static final String SURVIVAL_PERMISSION_NODE = "eessentials.gamemode.survival";

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
                        .then(argument("target", EntityArgumentType.player())  // Add player argument
                                .suggests((ctx, builder) -> {
                                    return CommandSource.suggestMatching(ctx.getSource().getServer().getPlayerNames(), builder);
                                })
                                .executes(ctx -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                                    return executeGameMode(ctx, GameMode.CREATIVE, target);  // Pass the target player to the execute method
                                }))
                        .executes(ctx -> executeGameMode(ctx, GameMode.CREATIVE))  // This is for when no player argument is provided.
        );


        // Registers the /gms command for changing to Survival mode.
        dispatcher.register(
                literal("gms")
                        .requires(source -> hasPermission(source, SURVIVAL_PERMISSION_NODE))
                        .then(argument("target", EntityArgumentType.player())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(ctx.getSource().getServer().getPlayerNames(), builder))
                                .executes(ctx -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                                    return executeGameMode(ctx, GameMode.SURVIVAL, target);
                                }))
                        .executes(ctx -> executeGameMode(ctx, GameMode.SURVIVAL))
        );

        // Registers the /gmsp command for changing to Spectator mode.
        dispatcher.register(
                literal("gmsp")
                        .requires(source -> hasPermission(source, SPECTATOR_PERMISSION_NODE))
                        .then(argument("target", EntityArgumentType.player())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(ctx.getSource().getServer().getPlayerNames(), builder))
                                .executes(ctx -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                                    return executeGameMode(ctx, GameMode.SPECTATOR, target);
                                }))
                        .executes(ctx -> executeGameMode(ctx, GameMode.SPECTATOR))
        );

        // Registers the /gma command for changing to Adventure mode.
        dispatcher.register(
                literal("gma")
                        .requires(source -> hasPermission(source, ADVENTURE_PERMISSION_NODE))
                        .then(argument("target", EntityArgumentType.player())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(ctx.getSource().getServer().getPlayerNames(), builder))
                                .executes(ctx -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                                    return executeGameMode(ctx, GameMode.ADVENTURE, target);
                                }))
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
    private static int executeGameMode(CommandContext<ServerCommandSource> ctx, GameMode gameMode, ServerPlayerEntity... targets) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = targets.length > 0 ? targets[0] : source.getPlayer();

        if (player == null) return 0;

        player.changeGameMode(gameMode);
        String formattedGameModeName = formatGameModeName(gameMode);

        if (player.equals(source.getPlayer())) {
            player.sendMessage(Text.literal("Set own game mode to " + formattedGameModeName + " Mode.").formatted(Formatting.WHITE), false);
        } else {
            player.sendMessage(Text.literal("Your game mode has been set to " + formattedGameModeName + " Mode.").formatted(Formatting.WHITE), false);
            source.sendMessage(Text.literal("Set " + player.getName().getString() + "'s game mode to " + formattedGameModeName + " Mode.").formatted(Formatting.WHITE));
        }
        return 1;
    }

    /**
     * Formats the game mode name such that only the first letter is capitalized.
     *
     * @param gameMode The game mode to format.
     * @return The formatted game mode name.
     */
    private static String formatGameModeName(GameMode gameMode) {
        String name = gameMode.name().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
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

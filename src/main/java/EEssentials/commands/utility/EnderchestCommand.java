package EEssentials.commands.utility;

import EEssentials.util.PermissionHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import static net.minecraft.server.command.CommandManager.*;

import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.stat.Stats;

/**
 * Provides command to open the enderchest.
 */
public class EnderchestCommand {

    // Permission node for the enderchest command.
    public static final String ENDERCHEST_PERMISSION_NODE = "eessentials.enderchest";

    /**
     * Registers the enderchest command.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("enderchest")
                        .requires(source -> hasPermission(source, ENDERCHEST_PERMISSION_NODE))
                        .executes(ctx -> openEnderchest(ctx))  // Opens the enderchest for the executing player
                        .then(argument("target", EntityArgumentType.player())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(ctx.getSource().getServer().getPlayerNames(), builder))
                                .executes(ctx -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                                    return openEnderchest(ctx, target);  // Opens the enderchest for the specified player
                                }))
        );
    }

    /**
     * Opens the target player's enderchest.
     *
     * @param ctx The command context.
     * @param targets The target players.
     * @return 1 if successful, 0 otherwise.
     */
    private static int openEnderchest(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity... targets) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity executingPlayer = source.getPlayer();
        ServerPlayerEntity targetPlayer = targets.length > 0 ? targets[0] : executingPlayer;

        if (executingPlayer == null || targetPlayer == null) return 0;

        NamedScreenHandlerFactory screenHandlerFactory = new SimpleNamedScreenHandlerFactory(
                (syncId, inventory, p) ->
                        GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, targetPlayer.getEnderChestInventory()),
                Text.translatable("container.enderchest")
        );

        executingPlayer.openHandledScreen(screenHandlerFactory);
        executingPlayer.incrementStat(Stats.OPEN_ENDERCHEST);

        if (!executingPlayer.equals(targetPlayer)) {
            source.sendMessage(Text.of("Opening " + targetPlayer.getName().getString() + "'s enderchest."));
        }

        return 1;
    }


    /**
     * Checks if a player has the required permissions to execute a command.
     *
     * @param source The command source.
     * @param permissionNode The permission node for the command.
     * @return True if the player has permissions, false otherwise.
     */
    private static boolean hasPermission(ServerCommandSource source, String permissionNode) {
        return source.hasPermissionLevel(2) || PermissionHelper.hasPermission(source.getPlayer(), permissionNode);
    }
}

package EEssentials.commands.utility;

import EEssentials.screens.EnderchestScreen;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

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
                        .requires(Permissions.require(ENDERCHEST_PERMISSION_NODE, 2))
                        .executes(ctx -> openEnderchest(ctx))  // Opens the enderchest for the executing player
                        .then(argument("target", EntityArgumentType.player())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(ctx.getSource().getServer().getPlayerNames(), builder))
                                .executes(ctx -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                                    return openEnderchest(ctx, target);  // Opens the enderchest for the specified player
                                }))
        );

        // /echest is an alias for enderchest
        dispatcher.register(
                literal("echest")
                        .requires(Permissions.require(ENDERCHEST_PERMISSION_NODE, 2))
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

        EnderchestScreen screenHandlerFactory = new EnderchestScreen(targetPlayer);
        executingPlayer.openHandledScreen(screenHandlerFactory);
        executingPlayer.incrementStat(Stats.OPEN_ENDERCHEST);

        if (!executingPlayer.equals(targetPlayer)) {
            source.sendMessage(Text.of("Opening " + targetPlayer.getName().getString() + "'s enderchest."));
        }

        return 1;
    }

}

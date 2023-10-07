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
 * Provides command to feed the player.
 */
public class FeedCommand {

    // Permission node for the feed command.
    public static final String FEED_PERMISSION_NODE = "eessentials.feed";

    /**
     * Registers the feed command.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("feed")
                        .requires(Permissions.require(FEED_PERMISSION_NODE, 2))
                        .executes(ctx -> feedPlayer(ctx))  // Feeds the executing player
                        .then(argument("target", EntityArgumentType.player())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(ctx.getSource().getServer().getPlayerNames(), builder))
                                .executes(ctx -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                                    return feedPlayer(ctx, target);  // Feeds the specified player
                                }))
        );
    }

    /**
     * Feeds the target player.
     *
     * @param ctx The command context.
     * @param targets The target players.
     * @return 1 if successful, 0 otherwise.
     */
    private static int feedPlayer(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity... targets) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = targets.length > 0 ? targets[0] : source.getPlayer();

        if (player == null) return 0;

        // Set hunger and saturation to max
        player.getHungerManager().setFoodLevel(20); // Max hunger level is 20
        player.getHungerManager().setSaturationLevel(20); // Max saturation level is 20

        if (player.equals(source.getPlayer())) {
            player.sendMessage(Text.of("You have been fed."), false);
        } else {
            source.sendMessage(Text.of("Fed " + player.getName().getString() + "."));
        }

        return 1;
    }

}

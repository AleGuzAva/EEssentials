package EEssentials.commands.other;

import EEssentials.util.PermissionHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import static net.minecraft.server.command.CommandManager.*;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.stat.Stats;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import java.util.concurrent.TimeUnit;

/**
 * Provides command to display player's playtime.
 */
public class PlaytimeCommand {

    // Permission node for the playtime command.
    public static final String PLAYTIME_PERMISSION_NODE = "eessentials.playtime";

    /**
     * Registers the playtime command.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("playtime")
                        .requires(source -> hasPermission(source, PLAYTIME_PERMISSION_NODE))
                        .executes(ctx -> showPlaytime(ctx))  // Shows the executing player's playtime
                        .then(argument("target", EntityArgumentType.player())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(ctx.getSource().getServer().getPlayerNames(), builder))
                                .executes(ctx -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                                    return showPlaytime(ctx, target);  // Shows the specified player's playtime
                                }))
        );
    }

    /**
     * Displays the playtime of the target player.
     *
     * @param ctx The command context.
     * @param targets The target players.
     * @return 1 if successful, 0 otherwise.
     */
    private static int showPlaytime(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity... targets) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = targets.length > 0 ? targets[0] : source.getPlayer();

        if (player == null) return 0;

        int timePlayed = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));
        String timeString = formatTime(timePlayed);

        if (player.equals(source.getPlayer())) {
            player.sendMessage(Text.of("You've played for: " + timeString), false);
        } else {
            source.sendMessage(Text.of(player.getName().getString() + " has played for: " + timeString));
        }

        return 1;
    }

    /**
     * Formats the given time (in ticks) to a readable format.
     *
     * @param ticks The time in ticks.
     * @return The formatted time string.
     */
    private static String formatTime(int ticks) {
        long seconds = ticks / 20;
        long days = TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - (days * 24);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
        return String.format("%d days, %d hours, %d minutes", days, hours, minutes);
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

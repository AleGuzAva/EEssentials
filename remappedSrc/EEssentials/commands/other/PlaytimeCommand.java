package EEssentials.commands.other;

import EEssentials.lang.LangManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import static net.minecraft.server.command.CommandManager.*;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.stat.Stats;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Provides command to display player's playtime.
 */
public class PlaytimeCommand {

    // Permission nodes for the playtime command.
    public static final String PLAYTIME_SELF_PERMISSION_NODE = "eessentials.playtime.self";
    public static final String PLAYTIME_OTHER_PERMISSION_NODE = "eessentials.playtime.other";

    /**
     * Registers the playtime command.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("playtime")
                        .requires(Permissions.require(PLAYTIME_SELF_PERMISSION_NODE, 2))
                        .executes(PlaytimeCommand::showPlaytime)  // Shows the executing player's playtime
                        .then(argument("target", EntityArgumentType.player())
                                .requires(Permissions.require(PLAYTIME_OTHER_PERMISSION_NODE, 2))
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

        Map<String, String> replacements = new HashMap<>();
        replacements.put("{playtime}", timeString);

        if (player.equals(source.getPlayer())) {
            LangManager.send(player, "Playtime", replacements);
        } else {
            replacements.put("player", player.getName().getString());
            LangManager.send(source, "Playtime-Other", replacements);
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

}

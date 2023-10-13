package EEssentials.util;

import net.minecraft.server.network.ServerPlayerEntity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class IgnoreManager {

    // Maps a player's UUID to the set of UUIDs of the players they've ignored.
    private static final HashMap<UUID, HashSet<UUID>> ignoredPlayers = new HashMap<>();

    /**
     * Checks if a player has ignored another player.
     *
     * @param player The player doing the ignoring.
     * @param target The player being ignored.
     * @return True if player has ignored target, false otherwise.
     */
    public static boolean hasIgnored(ServerPlayerEntity player, ServerPlayerEntity target) {
        HashSet<UUID> ignoredSet = ignoredPlayers.get(player.getUuid());
        return ignoredSet != null && ignoredSet.contains(target.getUuid());
    }

    /**
     * Add a player to another player's ignore list.
     *
     * @param player The player who wants to ignore.
     * @param target The player to be ignored.
     */
    public static void ignorePlayer(ServerPlayerEntity player, ServerPlayerEntity target) {
        ignoredPlayers
                .computeIfAbsent(player.getUuid(), k -> new HashSet<>())
                .add(target.getUuid());
    }

    /**
     * Remove a player from another player's ignore list.
     *
     * @param player The player who wants to unignore.
     * @param target The player to be unignored.
     */
    public static void unignorePlayer(ServerPlayerEntity player, ServerPlayerEntity target) {
        HashSet<UUID> ignoredSet = ignoredPlayers.get(player.getUuid());
        if (ignoredSet != null) {
            ignoredSet.remove(target.getUuid());
        }
    }

    /**
     * Retrieves the set of UUIDs of the players that the specified player has ignored.
     *
     * @param player The player.
     * @return Set of UUIDs of ignored players.
     */
    public static Set<UUID> getIgnoredPlayers(ServerPlayerEntity player) {
        return ignoredPlayers.getOrDefault(player.getUuid(), new HashSet<>());
    }

}

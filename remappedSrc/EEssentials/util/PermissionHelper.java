package EEssentials.util;

import EEssentials.EEssentials;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * Provides utility functions for checking permissions using LuckPerms.
 * Primarily used to validate if a player has certain privileges within the EEssentials mod.
 */
public class PermissionHelper {

    public LuckPerms luckperms = LuckPermsProvider.get();

    public User getLuckPermsUser(ServerPlayerEntity player) {
        return luckperms.getPlayerAdapter(
                ServerPlayerEntity.class
        ).getUser(player);
    }

    /**
     *
     * Iterates over all nodes a player inherits (sorted by permssion weight, largest first). It filters these nodes
     * to grab the "eessentials.homes.<x>" variable where x is the maximum amount of homes.
     *
     * @param player ServerPlayerEntity
     * @return int, maximum amount of homes (default 0)
     */
    public int getMaxHomes(ServerPlayerEntity player) {
        List<String> homes = getLuckPermsUser(player).resolveInheritedNodes(QueryOptions.nonContextual()).stream()
                .filter(node -> node.getKey().startsWith("eessentials.homes."))
                .map(node -> node.getKey().substring("eessentials.homes.".length()))
                .toList();

        int maxHomes = 0;

        for (String home: homes) {
            try {
                int homeCount = Integer.parseInt(home);
                if (homeCount > maxHomes) {
                    maxHomes = homeCount;
                }
            } catch (NumberFormatException e) {
                EEssentials.LOGGER.error("Non integer provided via eessentials.homes.<x> permission node.");
            }
        }

        return maxHomes;
    }

}

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

    public Boolean hasPermission(ServerPlayerEntity player, String permission) {
        var user = getLuckPermsUser(player);
        var permData = user.getCachedData().getPermissionData();
        if (permData.checkPermission(permission).asBoolean()) return true;
        return permData.checkPermission("eessentials.admin").asBoolean();
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

        for (String home: homes) {
            try {
                return Integer.parseInt(home);
            } catch (NumberFormatException e) {
                EEssentials.LOGGER.error("Non integer provided via eesentials.homes.<x> permission node.");
            }
        }

        return 0;
    }

}

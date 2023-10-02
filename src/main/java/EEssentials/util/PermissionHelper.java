package EEssentials.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Provides utility functions for checking permissions using LuckPerms.
 * Primarily used to validate if a player has certain privileges within the EEssentials mod.
 */
public class PermissionHelper {

    /**
     * Checks if the given player has a specific permission or a related wildcard permission.
     *
     * @param player The player to check the permission for.
     * @param permission The permission string to verify.
     * @return true if the player has the permission or its corresponding wildcard permission, false otherwise.
     */
    public static boolean hasPermission(ServerPlayerEntity player, String permission) {
        if (hasSpecificPermission(player, permission)) {
            return true;
        }

        // Check for wildcard permission
        if (hasSpecificPermission(player, "eessentials.*")) {
            return true;
        }

        return false;
    }

    /**
     * Checks if the given player has a specific permission using LuckPerms.
     *
     * @param player The player to check the permission for.
     * @param permission The permission string to verify.
     * @return true if the player has the permission, false otherwise.
     */
    private static boolean hasSpecificPermission(ServerPlayerEntity player, String permission) {
        // Fetch the LuckPerms API instance
        LuckPerms luckPerms = LuckPermsProvider.get();

        // Fetch the user manager to get user data
        UserManager userManager = luckPerms.getUserManager();

        // Retrieve the LuckPerms user data for the given player
        User user = userManager.getUser(player.getUuid());

        if (user == null) {
            return false; // Player not found in LuckPerms data.
        }

        // Check and return if the user has the specific permission
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }
}

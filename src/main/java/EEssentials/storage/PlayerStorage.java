package EEssentials.storage;

import EEssentials.util.Location;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.UUID;

public class PlayerStorage {

    private UUID playerUUID;
    public final HashMap<String, Location> homes = new HashMap<>();

    public PlayerStorage(UUID uuid) {
        playerUUID = uuid;
    }

    public static PlayerStorage fromPlayer(ServerPlayerEntity player) {
        return new PlayerStorage(player.getUuid());
    }

}

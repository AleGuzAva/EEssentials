package EEssentials.storage;

import net.minecraft.server.network.ServerPlayerEntity;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

public class StorageManager {

    public final Path storageDirectory;
    public final Path playerStorageDirectory;

    public final WorldSpawns worldSpawns;
    private final HashMap<UUID, PlayerStorage> playerStores = new HashMap<>();


    public StorageManager(Path storageDirectory) {
        this.storageDirectory = storageDirectory;
        this.playerStorageDirectory = storageDirectory.resolve("player");
        this.playerStorageDirectory.toFile().mkdirs();
        this.worldSpawns = new WorldSpawns(storageDirectory.resolve("world-spawns.json"));
    }

    public void serverStarted() {
        this.worldSpawns.load();
    }

    public PlayerStorage getPlayerStorage(UUID uuid) {
        return playerStores.getOrDefault(uuid, new PlayerStorage(uuid));
    }

    public PlayerStorage getPlayerStorage(ServerPlayerEntity player) {
        return getPlayerStorage(player.getUuid());
    }

    public void playerJoined(ServerPlayerEntity player) {
        playerStores.putIfAbsent(player.getUuid(), PlayerStorage.fromPlayer(player));
    }

    public void playerLeft(ServerPlayerEntity player) {
        playerStores.remove(player.getUuid());
    }

}

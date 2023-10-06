package EEssentials.storage;

import EEssentials.EEssentials;
import EEssentials.util.Location;
import EEssentials.util.cereal.ServerWorldDeserializer;
import EEssentials.util.cereal.ServerWorldSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

public class PlayerStorage {

    private UUID playerUUID;
    public final HashMap<String, Location> homes = new HashMap<>();
    public Boolean playedBefore = false;

    public PlayerStorage(UUID uuid) {
        playerUUID = uuid;
        load();
    }

    public static PlayerStorage fromPlayer(ServerPlayerEntity player) {
        return new PlayerStorage(player.getUuid());
    }

    public File getSaveFile() {
        File file = EEssentials.storage.playerStorageDirectory.resolve(playerUUID.toString() + ".json").toFile();
        try {
            playedBefore = !file.createNewFile();
        } catch (IOException e) {
            EEssentials.LOGGER.error("Failed to create file for PlayerStorage /w UUID: " + playerUUID.toString());
        }
        return file;
    }

    private Gson createCustomGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(ServerWorld.class, new ServerWorldSerializer());
        builder.registerTypeAdapter(ServerWorld.class, new ServerWorldDeserializer());
        return builder.create();
    }

    public void save() {
        Gson gson = createCustomGson();

        try (Writer writer = new FileWriter(getSaveFile())) {
            gson.toJson(homes, writer);
        } catch (IOException e) {
            EEssentials.LOGGER.error("Failed to save homes for UUID: " + playerUUID.toString(), e);
        }
    }

    public void load() {
        Gson gson = createCustomGson();

        try (Reader reader = new FileReader(getSaveFile())) {
            HashMap<String, Location> loadedHomes = gson.fromJson(reader, new TypeToken<HashMap<String, Location>>() {}.getType());
            if (loadedHomes != null) {
                homes.clear();
                homes.putAll(loadedHomes);
            }
        } catch (IOException | JsonParseException e) {
            EEssentials.LOGGER.warn("Failed to load homes from file: " + getSaveFile().getName(), e);
        }
    }

}

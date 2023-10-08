package EEssentials.storage;

import EEssentials.EEssentials;
import EEssentials.util.Location;
import EEssentials.util.cereal.LocationDeserializer;
import EEssentials.util.cereal.LocationSerializer;
import EEssentials.util.cereal.ServerWorldDeserializer;
import EEssentials.util.cereal.ServerWorldSerializer;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

public class PlayerStorage {

    private UUID playerUUID;
    public final HashMap<String, Location> homes = new HashMap<>();
    public Boolean playedBefore = false;
    private Location previousLocation;

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
        builder.registerTypeAdapter(Location.class, new LocationSerializer());
        builder.registerTypeAdapter(Location.class, new LocationDeserializer());
        return builder.create();
    }

    public void setPreviousLocation(Location location) {
        this.previousLocation = location;
        this.save();
    }

    public Location getPreviousLocation() {
        return this.previousLocation;
    }

    public void save() {
        Gson gson = createCustomGson();

        try (Writer writer = new FileWriter(getSaveFile())) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("homes", gson.toJsonTree(homes));
            jsonObject.add("previousLocation", gson.toJsonTree(previousLocation));
            gson.toJson(jsonObject, writer);
        } catch (IOException e) {
            EEssentials.LOGGER.error("Failed to save data for UUID: " + playerUUID.toString(), e);
        }
    }

    public void load() {
        Gson gson = createCustomGson();

        try (Reader reader = new FileReader(getSaveFile())) {
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            if (jsonObject.has("homes")) {
                HashMap<String, Location> loadedHomes = gson.fromJson(jsonObject.get("homes"), new TypeToken<HashMap<String, Location>>() {}.getType());
                if (loadedHomes != null) {
                    homes.clear();
                    homes.putAll(loadedHomes);
                }
            }

            if (jsonObject.has("previousLocation")) {
                previousLocation = gson.fromJson(jsonObject.get("previousLocation"), Location.class);
            }

        } catch (IOException | JsonParseException e) {
            EEssentials.LOGGER.warn("Failed to load data from file: " + getSaveFile().getName(), e);
        }
    }
}

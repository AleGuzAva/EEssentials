package EEssentials.storage;

import EEssentials.EEssentials;
import EEssentials.util.Location;
import EEssentials.util.cereal.ServerWorldDeserializer;
import EEssentials.util.cereal.ServerWorldSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.world.ServerWorld;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;

public class WorldSpawns {

    public Location serverSpawn = null;
    public HashMap<String, Location> warps = new HashMap<>();

    private final Path storagePath;

    public WorldSpawns(Path storagePath) {
        this.storagePath = storagePath;
    }

    public void setSpawn(Location loc) {
        this.serverSpawn = loc;
        save();
    }

    public File getSaveFile() {
        File file = storagePath.toFile();
        try {
            file.createNewFile();
        } catch (IOException e) {
            EEssentials.LOGGER.error("Failed to create World Spawns file (/config/storage/world-spawns.json");
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
            HashMap<String, Object> data = new HashMap<>();
            data.put("spawn", serverSpawn);
            data.put("warps", warps);
            gson.toJson(data, writer);
        } catch (IOException e) {
            EEssentials.LOGGER.error("Failed to save world-spawns.json", e);
        }
    }

    public void load() {
        Gson gson = createCustomGson();

        try (Reader reader = new FileReader(getSaveFile())) {
            Type type = new TypeToken<HashMap<String, Object>>() {}.getType();
            HashMap<String, Object> data = gson.fromJson(reader, type);
            if (data == null) return;

            EEssentials.LOGGER.info(data.toString());

            serverSpawn = gson.fromJson(gson.toJson(data.get("spawn")), Location.class);
            Type warpType = new TypeToken<HashMap<String, Location>>() {}.getType();
            HashMap<String, Location> loadedWarps = gson.fromJson(data.get("warps").toString(), warpType);
            if (loadedWarps != null) {
                warps = loadedWarps;
            }
        } catch (NullPointerException | IOException | JsonParseException e) {
            EEssentials.LOGGER.warn("Failed to load WorldSpawns from file: " + getSaveFile().getName(), e);
        }
    }
}

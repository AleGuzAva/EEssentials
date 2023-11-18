package EEssentials.storage;

import EEssentials.EEssentials;
import EEssentials.util.Location;
import EEssentials.util.cereal.ServerWorldDeserializer;
import EEssentials.util.cereal.ServerWorldSerializer;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.server.world.ServerWorld;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

/**
 * Manages the server's warp points and spawn locations.
 */
public class LocationManager {

    // Represents the main server spawn location
    public Location serverSpawn = null;

    // A map storing all warp points, mapped by their name
    public Map<String, Location> warps = new HashMap<>();

    // A list that stores Mod IDs for mods that have had their data imported
    public List<String> modImports = new ArrayList<>();

    // Path to the file where location data is saved
    private final Path storagePath;


    /**
     * Initializes the LocationManager with a given storage path.
     * @param storagePath The path to the file where data should be saved.
     */
    public LocationManager(Path storagePath) {
        this.storagePath = storagePath;
    }

    /**
     * Set the server's spawn location.
     * @param loc The spawn location.
     */
    public void setSpawn(Location loc) {
        this.serverSpawn = loc;
        save();
    }

    /**
     * Set or update a warp point.
     * @param warpName The name of the warp.
     * @param loc The location of the warp.
     */
    public void setWarp(String warpName, Location loc) {
        this.warps.put(warpName, loc);
        save();
    }

    /**
     * Retrieve a warp location by its name.
     * @param warpName The name of the warp.
     * @return The location of the warp or null if not found.
     */
    public Location getWarp(String warpName) {
        return this.warps.get(warpName);
    }

    /**
     * Deletes a warp point.
     * @param warpName The name of the warp to delete.
     */
    public void deleteWarp(String warpName) {
        this.warps.remove(warpName);
        save();
    }

    /**
     * @return A set containing the names of all warp points.
     */
    public Set<String> getWarpNames() {
        return Collections.unmodifiableSet(this.warps.keySet());
    }

    /**
     * Retrieve the save file, creating it if it doesn't exist.
     * @return The file object representing the save file.
     */
    public File getSaveFile() {
        File file = storagePath.toFile();
        try {
            file.createNewFile();
        } catch (IOException e) {
            EEssentials.LOGGER.error("Failed to create World Spawns file (/config/EEssentials/Locations.json", e);
        }
        return file;
    }


    /**
     * Retrieve the save file, creating it if it doesn't exist.
     * @return The file object representing the save file.
     */
    private Gson createCustomGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(ServerWorld.class, new ServerWorldSerializer());
        builder.registerTypeAdapter(ServerWorld.class, new ServerWorldDeserializer());
        return builder.create();
    }

    /**
     * Save the current warp points and server spawn location to file.
     */
    public void save() {
        Gson gson = createCustomGson();

        try (Writer writer = new FileWriter(getSaveFile())) {
            HashMap<String, Object> data = new HashMap<>();
            data.put("spawn", serverSpawn);
            data.put("warps", warps);
            data.put("modImports", modImports);
            gson.toJson(data, writer);
        } catch (IOException e) {
            EEssentials.LOGGER.error("Failed to save Locations.json", e);
        }
    }

    /**
     * Loads warp points and server spawn location from file.
     * If the file doesn't exist or is corrupted, initializes with default values.
     */
    public void load() {
        Gson gson = createCustomGson();

        try (Reader reader = new FileReader(getSaveFile())) {
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            if (jsonObject == null) {
                jsonObject = new JsonObject();
            }

            if (jsonObject.has("warps")) {
                HashMap<String, Location> loadedWarps = gson.fromJson(jsonObject.get("warps"), new TypeToken<HashMap<String, Location>>() {}.getType());
                if (loadedWarps != null) {
                    this.warps.clear();
                    this.warps.putAll(loadedWarps);
                }
            }

            if (jsonObject.has("spawn")) {
                this.serverSpawn = gson.fromJson(jsonObject.get("spawn"), Location.class);
            }

            if (jsonObject.has("modImports")) {
                this.modImports.clear();
                this.modImports = gson.fromJson(jsonObject.get("modImports"), new TypeToken<ArrayList<String>>() {}.getType());
            }
        } catch (IOException | JsonParseException e) {
            EEssentials.LOGGER.warn("Failed to load /config/EEssentials/Locations.json. File might be corrupt or missing.", e);
        }
    }

}

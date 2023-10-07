package EEssentials.util.cereal;

import EEssentials.util.Location;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import net.minecraft.server.world.ServerWorld;

public class LocationSerializer implements JsonSerializer<Location> {
    @Override
    public JsonElement serialize(Location location, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("x", location.getX());
        jsonObject.addProperty("y", location.getY());
        jsonObject.addProperty("z", location.getZ());
        jsonObject.addProperty("yaw", location.getYaw());
        jsonObject.addProperty("pitch", location.getPitch());
        jsonObject.add("world", context.serialize(location.getWorld(), ServerWorld.class));

        return jsonObject;
    }
}



package EEssentials.util.cereal;

import EEssentials.util.Location;
import com.google.gson.*;
import net.minecraft.server.world.ServerWorld;

import java.lang.reflect.Type;
public class LocationDeserializer implements JsonDeserializer<Location> {
    @Override
    public Location deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        double x = jsonObject.get("x").getAsDouble();
        double y = jsonObject.get("y").getAsDouble();
        double z = jsonObject.get("z").getAsDouble();
        float yaw = jsonObject.get("yaw").getAsFloat();
        float pitch = jsonObject.get("pitch").getAsFloat();
        ServerWorld world = context.deserialize(jsonObject.get("world"), ServerWorld.class);

        return new Location(world, x, y, z, yaw, pitch);
    }
}
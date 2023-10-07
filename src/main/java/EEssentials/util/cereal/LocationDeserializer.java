package EEssentials.util.cereal;

import EEssentials.util.Location;
import com.google.gson.*;
import net.minecraft.server.world.ServerWorld;

import java.lang.reflect.Type;
public class LocationDeserializer implements JsonDeserializer<Location> {
    @Override
    public Location deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        double x = jsonObject.getAsJsonPrimitive("x").getAsDouble();
        double y = jsonObject.getAsJsonPrimitive("y").getAsDouble();
        double z = jsonObject.getAsJsonPrimitive("z").getAsDouble();
        float yaw = jsonObject.getAsJsonPrimitive("yaw").getAsFloat();
        float pitch = jsonObject.getAsJsonPrimitive("pitch").getAsFloat();
        ServerWorld world = context.deserialize(jsonObject.get("world"), ServerWorld.class);

        return new Location(world, x, y, z, yaw, pitch); // Pass yaw and pitch to the constructor
    }
}



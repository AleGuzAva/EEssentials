package EEssentials.util.cereal;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.server.world.ServerWorld;

import java.lang.reflect.Type;

public class ServerWorldSerializer implements JsonSerializer<ServerWorld> {
    @Override
    public JsonElement serialize(ServerWorld world, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(world.getRegistryKey().getValue().toString());
    }
}
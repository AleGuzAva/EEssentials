package EEssentials.util.cereal;

import EEssentials.EEssentials;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.lang.reflect.Type;

public class ServerWorldDeserializer implements JsonDeserializer<ServerWorld> {
    @Override
    public ServerWorld deserialize(JsonElement worldIdentifier, Type type, JsonDeserializationContext context) throws JsonParseException {
        return EEssentials.server.getWorld(
                RegistryKey.of(
                        RegistryKeys.WORLD,
                        Identifier.tryParse(worldIdentifier.getAsString())
                )
        );
    }
}
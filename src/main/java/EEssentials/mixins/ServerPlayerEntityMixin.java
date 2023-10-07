package EEssentials.mixins;

import EEssentials.EEssentials;
import EEssentials.util.Location;
import EEssentials.storage.PlayerStorage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Shadow
    public abstract boolean isSpectator();

    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V", at = @At("HEAD"))
    public void savePreviousLocationBeforeTeleport(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        if (!player.isSpectator()) {
            // Save the current position before the teleportation starts
            PlayerStorage storage = EEssentials.storage.getPlayerStorage(player);
            storage.setPreviousLocation(new Location(player.getServerWorld(), player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch()));
            storage.save();
        }
    }
}


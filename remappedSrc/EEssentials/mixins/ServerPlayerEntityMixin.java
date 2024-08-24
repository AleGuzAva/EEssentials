package EEssentials.mixins;

import EEssentials.EEssentials;
import EEssentials.storage.PlayerStorage;
import EEssentials.util.Location;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)

public class ServerPlayerEntityMixin {
    @Inject(method = "onDeath", at = @At("HEAD"))
    public void onDeath(DamageSource damageSource, CallbackInfo callbackInfo) {
        PlayerStorage storage = EEssentials.storage.getPlayerStorage((ServerPlayerEntity) (Object) this);
        storage.setPreviousLocation(Location.fromPlayer((ServerPlayerEntity) (Object) this));
    }
}

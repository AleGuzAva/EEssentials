package EEssentials.mixins;

import EEssentials.events.ServerTickCallback;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class ServerTickMixin {

    @Inject(at = @At("HEAD"), method = "tick")
    private void onTick(CallbackInfo ci) {
        ServerTickCallback.EVENT.invoker().onTick();
    }
}

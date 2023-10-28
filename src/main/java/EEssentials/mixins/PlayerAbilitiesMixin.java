package EEssentials.mixins;

import net.minecraft.entity.player.PlayerAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerAbilities.class)
public interface PlayerAbilitiesMixin {

    @Accessor("flySpeed")
    void setFlySpeed(float flySpeed);

    @Accessor("flySpeed")
    float getFlySpeed();

    @Accessor("walkSpeed")
    void setWalkSpeed(float walkSpeed);

    @Accessor("walkSpeed")
    float getWalkSpeed();
}

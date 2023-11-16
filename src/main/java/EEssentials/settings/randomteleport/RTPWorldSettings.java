package EEssentials.settings.randomteleport;

import EEssentials.EEssentials;
import EEssentials.config.Configuration;
import EEssentials.util.RandomHelper;
import EEssentials.util.cooldown.CooldownHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class RTPWorldSettings {
    private final String worldName;
    private final int minDistance;
    private final int maxDistance;
    private final int cooldown;
    private final boolean allowCaveTeleports;
    private final int highestY;

    public RTPWorldSettings(String worldName, Configuration worldConfig) {
        this.worldName = worldName;
        this.minDistance = worldConfig.getInt("Minimum-Distance", 250);
        this.maxDistance = worldConfig.getInt("Maximum-Distance", 5000);
        this.cooldown = worldConfig.getInt("Cooldown", 30);
        this.allowCaveTeleports = worldConfig.getBoolean("Allow-Cave-Teleports", false);
        this.highestY = worldConfig.getInt("Highest-Y", 320);
    }

    public int getRandomIntInBounds() {
        int position = RandomHelper.randomIntBetween(-this.maxDistance, this.maxDistance);
        while(position > -this.minDistance && position < this.minDistance) {
            position+=RandomHelper.randomIntBetween(-this.minDistance, this.minDistance);
        }
        return position;
    }

    public boolean allowCaveTeleports() {
        return this.allowCaveTeleports;
    }

    public long getPlayerCooldown(ServerPlayerEntity player) {
        return CooldownHelper.getCooldown(player.getUuid(), this.worldName + "-RTP");
    }

    public void startPlayerCooldown(ServerPlayerEntity player) {
        CooldownHelper.startCooldown(player.getUuid(), this.worldName + "-RTP", this.cooldown);
    }

    public int getHighestY() {
        return highestY;
    }

    public ServerWorld getWorld() {
        for(ServerWorld world : EEssentials.server.getWorlds()) {
            if(world.getRegistryKey().getValue().toString().equals(this.worldName)) return world;
        }
        return null;
    }
}

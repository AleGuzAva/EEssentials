package EEssentials.util.cooldown;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CooldownHelper {
    private static final Map<UUID, CooldownData> playerCooldownData = new HashMap<>();

    public static long getCooldown(UUID playerUUID, String cooldownKey) {
        CooldownData playerCooldownData = CooldownHelper.playerCooldownData.computeIfAbsent(playerUUID, uuid -> new CooldownData());

        return playerCooldownData.getCooldown(cooldownKey);
    }

    public static void startCooldown(UUID playerUUID, String cooldownKey, int cooldown) {
        CooldownData playerCooldownData = CooldownHelper.playerCooldownData.computeIfAbsent(playerUUID, uuid -> new CooldownData());

        long endTime = System.currentTimeMillis()+TimeUnit.SECONDS.toMillis(cooldown);
        playerCooldownData.setEndTime(cooldownKey, endTime);
    }
}

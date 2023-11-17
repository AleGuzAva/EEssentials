package EEssentials.util.cooldown;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CooldownData {
    private final Map<String, Long> endCooldownTimes = new HashMap<>();

    public CooldownData() {
    }

    public void setEndTime(String key, long lastUse) {
        this.endCooldownTimes.put(key, lastUse);
    }

    public long getCooldown(String key) {
        if(this.endCooldownTimes.containsKey(key)) {
            long now = System.currentTimeMillis();
            long endTime = this.endCooldownTimes.get(key);
            if(endTime-now <= 0) {
                return 0;
            } else {
                return (int) TimeUnit.MILLISECONDS.toSeconds(endTime-now);
            }
        } else return 0;
    }
}

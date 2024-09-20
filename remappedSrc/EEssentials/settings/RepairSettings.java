package EEssentials.settings;

import EEssentials.config.Configuration;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;

import java.util.List;

public abstract class RepairSettings {
    private static List<String> blacklistedItems;

    public static boolean isBlacklisted(ItemStack item) {
        String itemID = Registries.ITEM.getId(item.getItem()).toString();
        NbtCompound itemNbt = item.getNbt();
        if (itemNbt != null) {
            int customModelData = itemNbt.getInt("CustomModelData");
            return blacklistedItems.contains(itemID) || blacklistedItems.contains(itemID + ":" + customModelData);
        } else return blacklistedItems.contains(itemID);
    }

    public static void reload(Configuration repairConfig) {
        blacklistedItems = repairConfig.getStringList("Blacklisted-Items");
    }
}

package EEssentials.settings;

import EEssentials.config.Configuration;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;

import java.util.List;

public abstract class RepairSettings {
    private static List<String> blacklistedItems;

    public static boolean isBlacklisted(ItemStack item) {
        String itemID = Registries.ITEM.getId(item.getItem()).toString();
        ComponentMap itemNbt = item.getComponents();
        if (itemNbt != null) {
            CustomModelDataComponent customModelDataComponent = item.get(DataComponentTypes.CUSTOM_MODEL_DATA);
            int customModelData = customModelDataComponent != null ? customModelDataComponent.value() : 0;
            return blacklistedItems.contains(itemID) || blacklistedItems.contains(itemID + ":" + customModelData);
        } else return blacklistedItems.contains(itemID);
    }

    public static void reload(Configuration repairConfig) {
        blacklistedItems = repairConfig.getStringList("Blacklisted-Items");
    }
}

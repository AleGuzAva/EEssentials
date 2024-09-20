package EEssentials.screens;

import EEssentials.util.ViewOnlySlot;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

public class InventoryScreen extends SimpleGui {
    private final ServerPlayerEntity targetPlayer;
    private boolean canEdit = true;

    public InventoryScreen(ScreenHandlerType<?> type, ServerPlayerEntity player, ServerPlayerEntity targetPlayer) {
        super(type, player, false);
        this.targetPlayer = targetPlayer;
    }


    private void initializeInventory() {
        // Conditional slot redirection based on edit mode
        for (int i = 0; i < 4; i++) {
            int slotIndex = 39 - i; // Armor slots are 36-39
            this.setSlotRedirect(i, canEdit ? new Slot(targetPlayer.getInventory(), slotIndex, 0, 0) : new ViewOnlySlot(targetPlayer.getInventory(), slotIndex));
        }
        this.setSlotRedirect(4, canEdit ? new Slot(targetPlayer.getInventory(), 40, 0, 0) : new ViewOnlySlot(targetPlayer.getInventory(), 40)); // Offhand slot is 40

        // Add the main inventory slots
        for (int i = 9; i < 36; i++) {
            this.setSlotRedirect(i, canEdit ? new Slot(targetPlayer.getInventory(), i, 0, 0) : new ViewOnlySlot(targetPlayer.getInventory(), i));
        }

        // Add the hotbar slots
        for (int i = 0; i < 9; i++) {
            this.setSlotRedirect(36 + i, canEdit ? new Slot(targetPlayer.getInventory(), i, 0, 0) : new ViewOnlySlot(targetPlayer.getInventory(), i));
        }
    }

    public void setEditMode(boolean canEdit) {
        this.canEdit = canEdit;
        // Re-initialize inventory with the correct edit mode
        this.initializeInventory();
        // Unlock the player inventory if in edit mode, lock it otherwise
        this.setLockPlayerInventory(!canEdit);
    }

    @Override
    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
        if (this.canEdit) {
            return super.onClick(index, type, action, element);
        } else {
            return false;
        }
    }

    @Override
    public void onClose() {}

}

package EEssentials.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.entity.player.PlayerEntity;

public class ViewOnlySlot extends Slot {
    public ViewOnlySlot(Inventory inventory, int index) {
        super(inventory, index, 0, 0);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return false;
    }

    @Override
    public boolean canTakePartial(PlayerEntity player) {
        return false;
    }

    @Override
    public ItemStack takeStack(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertStack(ItemStack stack, int count) {
        return stack;
    }

    @Override
    public void setStack(ItemStack stack) {

    }

    @Override
    public void setStackNoCallbacks(ItemStack stack) {

    }
}


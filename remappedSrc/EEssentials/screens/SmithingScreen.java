package EEssentials.screens;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.screen.slot.Slot;

public class SmithingScreen extends SmithingScreenHandler {

    public SmithingScreen(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(syncId, playerInventory, context);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);

        // Dump the items from the crafting grid back to the player or drop them on the ground
        for (int i = 3; i < 3; i++) {
            Slot slot = this.getSlot(i);
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) {
                if (!player.giveItemStack(stack)) {
                    player.dropItem(stack, false);
                }
                slot.setStack(ItemStack.EMPTY);
            }
        }
    }
}

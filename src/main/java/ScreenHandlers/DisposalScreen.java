package ScreenHandlers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;

public class DisposalScreen extends GenericContainerScreenHandler {
    private final SimpleInventory disposalInventory;

    public DisposalScreen(int syncId, PlayerInventory playerInventory) {
        super(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, new SimpleInventory(9 * 6), 6); // 9x6 disposal inventory
        this.disposalInventory = (SimpleInventory) this.getInventory();
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        // Clear the disposal inventory when closed
        for (int i = 0; i < disposalInventory.size(); i++) {
            disposalInventory.removeStack(i);
        }
        player.sendMessage(Text.of("Trash cleared!"), false);
    }
}

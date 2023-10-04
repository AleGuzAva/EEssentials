package EEssentials.screens;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

public class EnderchestScreen implements NamedScreenHandlerFactory {
    private final PlayerEntity targetPlayer;

    public EnderchestScreen(PlayerEntity targetPlayer) {
        this.targetPlayer = targetPlayer;
    }

    @Override
    public Text getDisplayName() {
        return Text.of("Ender Chest");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return GenericContainerScreenHandler.createGeneric9x3(syncId, inv, targetPlayer.getEnderChestInventory());
    }
}

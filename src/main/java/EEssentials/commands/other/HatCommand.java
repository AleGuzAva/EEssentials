package EEssentials.commands.other;

import EEssentials.commands.AliasedCommand;
import EEssentials.lang.LangManager;
import EEssentials.settings.HatSettings;
import EEssentials.util.ColorUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.TranslatableComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.literal;

public class HatCommand {
    public static final String HAT_PERMISSION_NODE = "eessentials.hat";
    public static final String HAT_BLACKLIST_BYPASS_NODE = "eessentials.hat.bypass";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        new AliasedCommand() {
            @Override
            public LiteralCommandNode<ServerCommandSource> register(CommandDispatcher<ServerCommandSource> dispatcher) {
                Map<String, String> replacements = new HashMap<>();
                return dispatcher.register(literal("hat")
                        .requires(source -> Permissions.check(source, HAT_PERMISSION_NODE, 0))
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if(player != null) {
                                PlayerInventory playerInv = player.getInventory();
                                int selectedSlot = playerInv.selectedSlot;
                                ItemStack heldItem = playerInv.main.get(selectedSlot);
                                if(heldItem.isEmpty()) {
                                    LangManager.send(player, "Hat-Hand-Empty-Message");
                                } else if(
                                        Permissions.check(player, HAT_BLACKLIST_BYPASS_NODE, 1) ||
                                                !HatSettings.isBlacklisted(heldItem)){
                                    ItemStack headItem = playerInv.armor.get(3);
                                    playerInv.armor.set(3, heldItem);
                                    playerInv.main.set(selectedSlot, headItem);
                                    replacements.put("{item-hover}", ColorUtil.toMiniItemHover(heldItem));
                                    replacements.put("{item-name}", ColorUtil.componentToString(heldItem.getName().asComponent()));
                                    replacements.put("{item-name-unformatted}", heldItem.getName().getString());
                                    replacements.put("{item-type}", "<lang:" + ((TranslatableComponent)heldItem.getItem().getName().asComponent()).key() + ">");
                                    LangManager.send(player, "Hat-Equipped-Message", replacements);
                                } else {
                                    LangManager.send(player, "Hat-Blacklisted-Message");
                                }
                            } else {
                                LangManager.send(context.getSource(), "Invalid-Player-Only");
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                );
            }

            @Override
            public String[] getCommandAliases() {
                return new String[]{"head"};
            }
        }.registerWithAliases(dispatcher);
    }
}

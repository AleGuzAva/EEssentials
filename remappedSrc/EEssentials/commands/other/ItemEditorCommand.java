package EEssentials.commands.other;

import EEssentials.lang.LangManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import eu.pb4.placeholders.api.TextParserUtils;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Provides commands to edit the name and lore of the item in the player's hand.
 */
public class ItemEditorCommand {

    // Permission nodes for the item editor commands.
    public static final String ITEM_EDITOR_NAME_PERMISSION_NODE = "eessentials.itemeditor.name";
    public static final String ITEM_EDITOR_LORE_PERMISSION_NODE = "eessentials.itemeditor.lore";

    /**
     * Registers the item editor commands.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("itemeditor")
                        .then(literal("name")
                                .requires(Permissions.require(ITEM_EDITOR_NAME_PERMISSION_NODE, 2))
                                .then(argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> renameItem(ctx, StringArgumentType.getString(ctx, "name")))))
                        .then(literal("lore")
                                .requires(Permissions.require(ITEM_EDITOR_LORE_PERMISSION_NODE, 2))
                                .then(literal("add")
                                        .then(argument("lore", StringArgumentType.greedyString())
                                                .executes(ctx -> addItemLore(ctx, StringArgumentType.getString(ctx, "lore")))))
                                .then(literal("edit")
                                        .then(argument("line", IntegerArgumentType.integer(1))
                                                .then(argument("lore", StringArgumentType.greedyString())
                                                        .executes(ctx -> editItemLore(ctx, IntegerArgumentType.getInteger(ctx, "line"), StringArgumentType.getString(ctx, "lore"))))))
                                .then(literal("delete")
                                        .then(argument("line", IntegerArgumentType.integer(1))
                                                .executes(ctx -> deleteItemLore(ctx, IntegerArgumentType.getInteger(ctx, "line"))))))
                        .then(literal("reset")
                                .requires(Permissions.require(ITEM_EDITOR_NAME_PERMISSION_NODE, 2).or(Permissions.require(ITEM_EDITOR_LORE_PERMISSION_NODE, 2)))
                                .executes(ItemEditorCommand::resetItem))
        );
    }

    /**
     * Renames the item in the player's hand.
     *
     * @param ctx The command context.
     * @param name The new name for the item.
     * @return 1 if successful, 0 otherwise.
     * @throws CommandSyntaxException If the command execution fails.
     */
    private static int renameItem(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            LangManager.send(ctx.getSource(), "Invalid-Player-Only");
            return 0;
        }

        ItemStack itemStack = player.getStackInHand(Hand.MAIN_HAND);
        if (itemStack.isEmpty()) {
            LangManager.send(ctx.getSource(), "Invalid-Item-In-Hand");
            return 0;
        }

        if (!Permissions.check(player, ITEM_EDITOR_NAME_PERMISSION_NODE, false)) {
            LangManager.send(ctx.getSource(), "Invalid-No-Permission");
            return 0;
        }

        try {
            MutableText newName = TextParserUtils.formatText(name).copy();
            itemStack.setCustomName(newName);
            LangManager.send(ctx.getSource(), "Item-Name-Success");
            return 1;
        } catch (Exception e) {
            LangManager.send(ctx.getSource(), "Item-Name-Error");
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Adds lore to the item in the player's hand.
     *
     * @param ctx The command context.
     * @param lore The new lore for the item.
     * @return 1 if successful, 0 otherwise.
     * @throws CommandSyntaxException If the command execution fails.
     */
    private static int addItemLore(CommandContext<ServerCommandSource> ctx, String lore) throws CommandSyntaxException {
        return modifyLore(ctx, -1, lore);
    }

    /**
     * Edits the lore of the item in the player's hand.
     *
     * @param ctx The command context.
     * @param line The line number to edit.
     * @param lore The new lore for the item.
     * @return 1 if successful, 0 otherwise.
     * @throws CommandSyntaxException If the command execution fails.
     */
    private static int editItemLore(CommandContext<ServerCommandSource> ctx, int line, String lore) throws CommandSyntaxException {
        return modifyLore(ctx, line, lore);
    }

    /**
     * Deletes lore from the item in the player's hand.
     *
     * @param ctx The command context.
     * @param line The line number to delete.
     * @return 1 if successful, 0 otherwise.
     * @throws CommandSyntaxException If the command execution fails.
     */
    private static int deleteItemLore(CommandContext<ServerCommandSource> ctx, int line) throws CommandSyntaxException {
        return modifyLore(ctx, line, null);
    }

    /**
     * Modifies the lore of the item in the player's hand.
     *
     * @param ctx The command context.
     * @param line The line number to modify (-1 for add).
     * @param lore The new lore for the item (null for delete).
     * @return 1 if successful, 0 otherwise.
     * @throws CommandSyntaxException If the command execution fails.
     */
    private static int modifyLore(CommandContext<ServerCommandSource> ctx, int line, String lore) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            LangManager.send(ctx.getSource(), "Invalid-Player-Only");
            return 0;
        }

        ItemStack itemStack = player.getStackInHand(Hand.MAIN_HAND);
        if (itemStack.isEmpty()) {
            LangManager.send(ctx.getSource(), "Invalid-Player-Only");
            return 0;
        }

        if (!Permissions.check(player, ITEM_EDITOR_LORE_PERMISSION_NODE, false)) {
            LangManager.send(ctx.getSource(), "Invalid-No-Permission");
            return 0;
        }

        try {
            NbtCompound displayTag = itemStack.getOrCreateSubNbt("display");
            NbtList loreList = displayTag.contains("Lore") ? displayTag.getList("Lore", NbtElement.STRING_TYPE) : new NbtList();

            if (lore == null) {
                if (line > 0 && line <= loreList.size()) {
                    loreList.remove(line - 1);
                } else {
                    LangManager.send(ctx.getSource(), "Item-Lore-Error");
                    return 0;
                }
            } else {
                MutableText loreText = TextParserUtils.formatTextSafe(lore).copy();

                if (line == -1) {
                    loreList.add(NbtString.of(Text.Serializer.toJson(loreText)));
                } else {
                    if (line > 0 && line <= loreList.size()) {
                        loreList.set(line - 1, NbtString.of(Text.Serializer.toJson(loreText)));
                    } else {
                        LangManager.send(ctx.getSource(), "Item-Lore-Error");
                        return 0;
                    }
                }
            }

            displayTag.put("Lore", loreList);
            LangManager.send(ctx.getSource(), "Item-Lore-Success");
            return 1;
        } catch (Exception e) {
            LangManager.send(ctx.getSource(), "Item-Lore-Error");
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Resets the item in the player's hand to its original state.
     *
     * @param ctx The command context.
     * @return 1 if successful, 0 otherwise.
     * @throws CommandSyntaxException If the command execution fails.
     */
    private static int resetItem(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            LangManager.send(player, "Invalid-Player-Only");
            return 0;
        }

        ItemStack itemStack = player.getStackInHand(Hand.MAIN_HAND);
        if (itemStack.isEmpty()) {
            LangManager.send(player, "Invalid-Player-Only");
            return 0;
        }

        if (!Permissions.check(player, ITEM_EDITOR_NAME_PERMISSION_NODE, false) && !Permissions.check(player, ITEM_EDITOR_LORE_PERMISSION_NODE, false)) {
            LangManager.send(ctx.getSource(), "Invalid-No-Permission");
            return 0;
        }

        try {
            NbtCompound displayTag = itemStack.getOrCreateSubNbt("display");

            // Remove custom name and lore
            displayTag.remove("Name");
            displayTag.remove("Lore");

            LangManager.send(player, "Item-Reset-Success");
            return 1;
        } catch (Exception e) {
            LangManager.send(player, "Item-Reset-Error");
            e.printStackTrace();
            return 0;
        }
    }
}

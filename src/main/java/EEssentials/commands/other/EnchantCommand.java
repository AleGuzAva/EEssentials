package EEssentials.commands.other;

import EEssentials.lang.LangManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class EnchantCommand {

    public static final String ENCHANT_RESTRICTED_PERMISSION_NODE = "eessentials.enchant.restricted";
    public static final String ENCHANT_UNRESTRICTED_PERMISSION_NODE = "eessentials.enchant.unrestricted";

    private static final SuggestionProvider<ServerCommandSource> ENCHANTMENT_SUGGESTIONS = (context, builder) -> {
        ServerPlayerEntity player = context.getSource().getPlayer();
        boolean unrestricted = Permissions.check(player, ENCHANT_UNRESTRICTED_PERMISSION_NODE, 2);

        return CommandSource.suggestMatching(
                Registries.ENCHANTMENT.stream()
                        .filter(enchantment -> unrestricted || enchantment.isAcceptableItem(player.getStackInHand(Hand.MAIN_HAND)))
                        .map(enchantment -> Registries.ENCHANTMENT.getId(enchantment).toString().replace("minecraft:", ""))
                        .collect(Collectors.toList()),
                builder);
    };

    private static final SuggestionProvider<ServerCommandSource> UNENCHANT_SUGGESTIONS = (context, builder) -> {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ItemStack itemStack = player.getStackInHand(Hand.MAIN_HAND);

        if (itemStack.isEmpty()) {
            return builder.buildFuture();
        }

        Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(itemStack);
        return CommandSource.suggestMatching(
                enchantments.keySet().stream()
                        .map(enchantment -> Registries.ENCHANTMENT.getId(enchantment).toString().replace("minecraft:", ""))
                        .collect(Collectors.toList()),
                builder);
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Unregister the vanilla /enchant command
        unregisterVanillaEnchantCommand(dispatcher);

        dispatcher.register(CommandManager.literal("enchant")
                .requires(src -> Permissions.check(src, ENCHANT_RESTRICTED_PERMISSION_NODE, 2) ||
                        Permissions.check(src, ENCHANT_UNRESTRICTED_PERMISSION_NODE, 2))
                .then(CommandManager.argument("enchantment", StringArgumentType.string())
                        .suggests(ENCHANTMENT_SUGGESTIONS)
                        .then(CommandManager.argument("level", IntegerArgumentType.integer(1))
                                .executes(ctx -> applyEnchantment(ctx, StringArgumentType.getString(ctx, "enchantment"), IntegerArgumentType.getInteger(ctx, "level"))))));

        dispatcher.register(CommandManager.literal("unenchant")
                .requires(src -> Permissions.check(src, ENCHANT_RESTRICTED_PERMISSION_NODE, 2) ||
                        Permissions.check(src, ENCHANT_UNRESTRICTED_PERMISSION_NODE, 2))
                .then(CommandManager.argument("enchantment", StringArgumentType.string())
                        .suggests(UNENCHANT_SUGGESTIONS)
                        .executes(ctx -> removeEnchantment(ctx, StringArgumentType.getString(ctx, "enchantment")))));
    }

    private static void unregisterVanillaEnchantCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        try {
            dispatcher.getRoot().getChildren().removeIf(literalCommandNode -> literalCommandNode.getName().equals("enchant"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int applyEnchantment(CommandContext<ServerCommandSource> ctx, String enchantmentName, int level) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        ItemStack itemStack = player.getStackInHand(Hand.MAIN_HAND);

        if (itemStack.isEmpty()) {
            LangManager.send(player, "Invalid-Item-In-Hand");
            return 0;
        }

        Identifier enchantmentId = new Identifier("minecraft", enchantmentName);
        Enchantment enchantment = Registries.ENCHANTMENT.get(enchantmentId);
        if (enchantment == null) {
            LangManager.send(player, "Invalid-Enchantment");
            return 0;
        }

        boolean hasUnrestrictedPermission = Permissions.check(player, ENCHANT_UNRESTRICTED_PERMISSION_NODE, 2);

        if (!hasUnrestrictedPermission && (!enchantment.isAcceptableItem(itemStack) || level > enchantment.getMaxLevel())) {
            LangManager.send(player, "Invalid-Enchantment-Or-Level");
            return 0;
        }

        if (hasUnrestrictedPermission && level > 255) {
            level = 255;
        }

        try {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(itemStack);
            enchantments.put(enchantment, level);
            EnchantmentHelper.set(enchantments, itemStack);

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{enchantment}", enchantmentName);
            replacements.put("{level}", String.valueOf(level));

            LangManager.send(player, "Enchantment-Success", replacements);
            return 1;
        } catch (Exception e) {
            LangManager.send(player, "Enchantment-Error");
            e.printStackTrace();
            return 0;
        }
    }

    private static int removeEnchantment(CommandContext<ServerCommandSource> ctx, String enchantmentName) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        ItemStack itemStack = player.getStackInHand(Hand.MAIN_HAND);

        if (itemStack.isEmpty()) {
            LangManager.send(player, "Invalid-Item-In-Hand");
            return 0;
        }

        Identifier enchantmentId = new Identifier("minecraft", enchantmentName);
        Enchantment enchantment = Registries.ENCHANTMENT.get(enchantmentId);
        if (enchantment == null) {
            LangManager.send(player, "Invalid-Enchantment");
            return 0;
        }

        try {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(itemStack);
            if (enchantments.containsKey(enchantment)) {
                enchantments.remove(enchantment);
                EnchantmentHelper.set(enchantments, itemStack);

                Map<String, String> replacements = new HashMap<>();
                replacements.put("{enchantment}", enchantmentName);

                LangManager.send(player, "Unenchantment-Success", replacements);
                return 1;
            } else {
                Map<String, String> replacements = new HashMap<>();
                replacements.put("{enchantment}", enchantmentName);

                LangManager.send(player, "Enchantment-Not-Present", replacements);
                return 0;
            }
        } catch (Exception e) {
            LangManager.send(player, "Enchantment-Error");
            e.printStackTrace();
            return 0;
        }
    }
}

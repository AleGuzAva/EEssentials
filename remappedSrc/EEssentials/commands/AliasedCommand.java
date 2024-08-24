package EEssentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

/**
 * A base for all EEssentials commands with aliases.
 */
public interface AliasedCommand {
    /**
     * Overwriting this method declares the logic of the command.
     * 
     * @return Your {@link CommandDispatcher#register(LiteralArgumentBuilder)} chained call.
     */
    LiteralCommandNode<ServerCommandSource> register(CommandDispatcher<ServerCommandSource> dispatcher);

    default LiteralCommandNode<ServerCommandSource> register(CommandDispatcher<ServerCommandSource> dispatcher,
                                                             CommandRegistryAccess registryAccess) {
        return register(dispatcher);
    }

    /**
     * @return An array of all aliases. This should not include the original command's name.
     */
    default String[] getCommandAliases() {
        return new String[0];
    }

    /**
     * Responsible for registering the command and its defined aliases.
     */
    default void registerWithAliases(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> command = register(dispatcher);
        for(String alias : getCommandAliases()) {
            LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal(alias)
                    .requires(command.getRequirement())
                    .executes(command.getCommand());
            for(CommandNode<ServerCommandSource> child : command.getChildren()) {
                builder.then(child);
            }
            dispatcher.register(builder);
        }
    }

    default void registerWithAliases(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<ServerCommandSource> command = register(dispatcher, registryAccess);
        for(String alias : getCommandAliases()) {
            LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal(alias)
                    .requires(command.getRequirement())
                    .executes(command.getCommand());
            for(CommandNode<ServerCommandSource> child : command.getChildren()) {
                builder.then(child);
            }
            dispatcher.register(builder);
        }
    }
}

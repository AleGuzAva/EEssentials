package EEssentials.commands.teleportation;

import EEssentials.lang.LangManager;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import EEssentials.util.Location;

import java.util.Map;

public class TPHereCommand {

    public static final String TPHERE_PERMISSION_NODE = "eessentials.tphere";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tphere")
                .requires(Permissions.require(TPHERE_PERMISSION_NODE, 2))
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes(ctx -> {
                            // Extract the command sender and target player.
                            ServerPlayerEntity sender = ctx.getSource().getPlayer();
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");

                            // Check for self-teleport attempts.
                            if (sender.equals(target)) {
                                LangManager.send(sender, "TPHere-Self-Teleport");
                                return 0;
                            }

                            // Use the Location utility to teleport the target to the sender.
                            Location senderLocation = Location.fromPlayer(sender);
                            senderLocation.teleport(target);

                            // Send a confirmation message to the sender.
                            LangManager.send(sender, "TPHere-Success-Sender", Map.of("{target}", target.getEntityName()));
                            LangManager.send(target, "TPHere-Success-Target", Map.of("{sender}", sender.getEntityName()));

                            return 1;
                        })
                )
        );
    }
}

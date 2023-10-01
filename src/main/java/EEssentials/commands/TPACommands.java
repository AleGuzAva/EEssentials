package EEssentials.commands;

import EEssentials.util.Location;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains teleport-related commands including sending, accepting, and denying teleportation requests.
 */
public class TPACommands {

    // Maps target players to their teleportation requesters.
    private static final Map<ServerPlayerEntity, ServerPlayerEntity> teleportRequests = new HashMap<>();

    /**
     * Registers the teleport commands: /tpa, /tpaccept, and /tpdeny.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Register /tpa <target>
        // Allows a player to send a teleportation request to another player.
        dispatcher.register(CommandManager.literal("tpa")
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity requester = ctx.getSource().getPlayer();
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");

                            teleportRequests.put(target, requester);

                            requester.sendMessage(Text.literal("You have sent a teleportation request to " + target.getName().getString() + "."), false);
                            target.sendMessage(Text.literal(requester.getName().getString() + " wants to teleport to you. Use /tpaccept to allow!"), false);

                            return 1;
                        })));

        // Register /tpaccept
        // Allows a player to accept a pending teleportation request.
        dispatcher.register(CommandManager.literal("tpaccept")
                .executes(ctx -> {
                    ServerPlayerEntity target = ctx.getSource().getPlayer();

                    if (teleportRequests.containsKey(target)) {
                        ServerPlayerEntity requester = teleportRequests.get(target);
                        Location targetLocation = new Location(target.getServerWorld(), target.getX(), target.getY(), target.getZ());
                        targetLocation.teleport(requester);

                        requester.sendMessage(Text.literal(target.getName().getString() + " accepted your request. You are now being teleported."), false);
                        target.sendMessage(Text.literal(requester.getName().getString() + " has teleported to you!"), false);

                        teleportRequests.remove(target);
                    } else {
                        target.sendMessage(Text.literal("No teleportation requests pending."), false);
                    }
                    return 1;
                }));

        // Register /tpdeny
        // Allows a player to deny a pending teleportation request.
        dispatcher.register(CommandManager.literal("tpdeny")
                .executes(ctx -> {
                    ServerPlayerEntity target = ctx.getSource().getPlayer();

                    if (teleportRequests.containsKey(target)) {
                        ServerPlayerEntity requester = teleportRequests.get(target);

                        requester.sendMessage(Text.literal(target.getName().getString() + " has denied your teleportation request."), false);
                        target.sendMessage(Text.literal("You have denied " + requester.getName().getString() + "'s teleportation request."), false);

                        teleportRequests.remove(target);
                    } else {
                        target.sendMessage(Text.literal("No teleportation requests pending."), false);
                    }
                    return 1;
                }));
    }
}

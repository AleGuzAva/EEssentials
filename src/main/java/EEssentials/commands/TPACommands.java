package EEssentials.commands;

import EEssentials.util.Location;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains teleport-related commands including sending, accepting, and denying teleportation requests.
 */
public class TPACommands {

    private static class TeleportRequest {
        final ServerPlayerEntity requester;
        final RequestType type;

        public TeleportRequest(ServerPlayerEntity requester, RequestType type) {
            this.requester = requester;
            this.type = type;
        }

        enum RequestType {
            TPA, TPAHERE
        }
    }

    // Maps target players to a List of their teleportation requesters.
    private static final Map<ServerPlayerEntity, List<TeleportRequest>> teleportRequests = new HashMap<>();

    /**
     * Registers the teleport commands: /tpa, /tpaccept, and /tpdeny.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        // Register /tpa <target>
        // Allows a player to request to teleport to another player.
        dispatcher.register(CommandManager.literal("tpa")
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity requester = ctx.getSource().getPlayer();
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");

                            if (requester.equals(target)) {
                                requester.sendMessage(Text.literal("You can't send teleport requests to yourself!"), false);
                                return 0;
                            }

                            removeExistingRequestFrom(requester, target);  // Remove existing requests

                            teleportRequests.computeIfAbsent(target, k -> new ArrayList<>())
                                    .add(new TeleportRequest(requester, TeleportRequest.RequestType.TPA));

                            requester.sendMessage(Text.literal("You have sent a teleportation request to " + target.getName().getString() + "."), false);
                            target.sendMessage(Text.literal(requester.getName().getString() + " wants to teleport to you. Use /tpaccept to allow."), false);

                            return 1;
                        })));

        // Register /tpahere <target>
        // Allows a player to request another player teleport to them.
        dispatcher.register(CommandManager.literal("tpahere")
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity requester = ctx.getSource().getPlayer();
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");

                            if (requester.equals(target)) {
                                requester.sendMessage(Text.literal("You can't send teleport requests to yourself!"), false);
                                return 0;
                            }

                            removeExistingRequestFrom(requester, target);  // Remove existing requests

                            teleportRequests.computeIfAbsent(target, k -> new ArrayList<>())
                                    .add(new TeleportRequest(requester, TeleportRequest.RequestType.TPAHERE));

                            requester.sendMessage(Text.literal("You have sent a request for " + target.getName().getString() + " to teleport to you."), false);
                            target.sendMessage(Text.literal(requester.getName().getString() + " wants you to teleport to them. Use /tpaccept to allow."), false);

                            return 1;
                        })));

        // Register /tpaccept
        // Allows a player to accept a pending teleportation request.
        dispatcher.register(CommandManager.literal("tpaccept")
                .executes(ctx -> {
                    ServerPlayerEntity target = ctx.getSource().getPlayer();

                    if (teleportRequests.containsKey(target) && !teleportRequests.get(target).isEmpty()) {
                        TeleportRequest request = teleportRequests.get(target).remove(0);
                        ServerPlayerEntity requester = request.requester;

                        if (request.type == TeleportRequest.RequestType.TPA) {
                            Location targetLocation = new Location(target.getServerWorld(), target.getX(), target.getY(), target.getZ());
                            targetLocation.teleport(requester);

                            requester.sendMessage(Text.literal("You have teleported to " + target.getName().getString() + "."), false);
                            target.sendMessage(Text.literal(requester.getName().getString() + " has teleported to you."), false);
                        } else {
                            Location requesterLocation = new Location(requester.getServerWorld(), requester.getX(), requester.getY(), requester.getZ());
                            requesterLocation.teleport(target);

                            requester.sendMessage(Text.literal(target.getName().getString() + " has teleported to you."), false);
                            target.sendMessage(Text.literal("You have teleported to " + requester.getName().getString() + "."), false);
                        }

                        if (teleportRequests.get(target).isEmpty()) {
                            teleportRequests.remove(target);
                        }
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

                    if (teleportRequests.containsKey(target) && !teleportRequests.get(target).isEmpty()) {
                        TeleportRequest request = teleportRequests.get(target).remove(0); // Remove and get the first request.
                        ServerPlayerEntity requester = request.requester;

                        requester.sendMessage(Text.literal(target.getName().getString() + " has denied your teleportation request."), false);
                        target.sendMessage(Text.literal("You have denied " + requester.getName().getString() + "'s teleportation request."), false);

                        if (teleportRequests.get(target).isEmpty()) {
                            teleportRequests.remove(target);
                        }
                    } else {
                        target.sendMessage(Text.literal("No teleportation requests pending."), false);
                    }

                    return 1;
                }));

        // Register /tpacancel
        // Allows a player to cancel an outgoing teleportation request.
        dispatcher.register(CommandManager.literal("tpacancel")
                .executes(ctx -> {
                    ServerPlayerEntity requester = ctx.getSource().getPlayer();
                    boolean requestCancelled = false;

                    // Iterate through all teleport requests.
                    for (Map.Entry<ServerPlayerEntity, List<TeleportRequest>> entry : teleportRequests.entrySet()) {
                        List<TeleportRequest> requests = entry.getValue();

                        // Find a matching TeleportRequest for the requester and remove it.
                        for (int i = 0; i < requests.size(); i++) {
                            if (requests.get(i).requester.equals(requester)) {
                                requests.remove(i);
                                requestCancelled = true;
                                requester.sendMessage(Text.literal("You've cancelled your teleportation request to " + entry.getKey().getName().getString() + "."), false);

                                if (requests.isEmpty()) {
                                    teleportRequests.remove(entry.getKey());
                                }
                                break;
                            }
                        }

                        if (requestCancelled) {
                            break;
                        }
                    }

                    if (!requestCancelled) {
                        requester.sendMessage(Text.literal("You don't have any pending teleportation requests."), false);
                    }

                    return 1;
                }));

    }

    private static void removeExistingRequestFrom(ServerPlayerEntity requester, ServerPlayerEntity target) {
        if (teleportRequests.containsKey(target)) {
            List<TeleportRequest> requests = teleportRequests.get(target);
            requests.removeIf(request -> request.requester.equals(requester));
            if (requests.isEmpty()) {
                teleportRequests.remove(target);
            }
        }
    }

}

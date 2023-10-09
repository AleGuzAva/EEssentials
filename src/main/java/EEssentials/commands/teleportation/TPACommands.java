package EEssentials.commands.teleportation;

import EEssentials.util.Location;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.*;

/**
 * Handles teleportation commands allowing players to send, accept,
 * and deny teleportation requests.
 */

public class TPACommands {

    // Define permission nodes for various commands.
    public static final String TPA_PERMISSION_NODE = "eessentials.tpa";
    public static final String TPAHERE_PERMISSION_NODE = "eessentials.tpahere";
    public static final String TPTOGGLE_PERMISSION_NODE = "eessentials.tptoggle";

    /**
     * Inner class to represent a teleport request.
     */
    private static class TeleportRequest {
        final ServerPlayerEntity requester;  // Player who sent the request
        final RequestType type;  // Type of request (TPA or TPAHERE)
        final long timestamp;  // Time when the request was created

        public TeleportRequest(ServerPlayerEntity requester, RequestType type) {
            this.requester = requester;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }

        // Enum to differentiate between TPA and TPAHERE requests.
        enum RequestType {
            TPA, TPAHERE
        }
    }

    // Set to store UUIDs of players who have turned off incoming teleport requests.
    private static final Set<UUID> teleportToggleOff = new HashSet<>();

    // Map to store teleport requests. Maps a target player to a list of requests they've received.
    private static final Map<ServerPlayerEntity, List<TeleportRequest>> teleportRequests = new HashMap<>();

    /**
     * Register commands related to teleportation.
     * This includes /tpa, /tpahere, /tpaccept, /tpdeny, /tpcancel, and /tptoggle.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        // Command to request teleportation to another player.
        dispatcher.register(CommandManager.literal("tpa")
                .requires(Permissions.require(TPA_PERMISSION_NODE, 2))
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity requester = ctx.getSource().getPlayer();
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");

                            // Check if the target has teleport requests toggled off.
                            if (teleportToggleOff.contains(target.getUuid())) {
                                requester.sendMessage(Text.literal(target.getName().getString() + " has incoming teleport requests disabled."), false);
                                return 0;
                            }

                            // Check if requester is the same as target.
                            if (requester.equals(target)) {
                                requester.sendMessage(Text.literal("You can't send teleport requests to yourself!"), false);
                                return 0;
                            }

                            // Remove any existing requests from the requester to the target.
                            removeExistingRequestFrom(requester, target);

                            // Add the new request.
                            teleportRequests.computeIfAbsent(target, k -> new ArrayList<>())
                                    .add(new TeleportRequest(requester, TeleportRequest.RequestType.TPA));

                            // Notify both players.
                            requester.sendMessage(Text.literal("You have sent a teleportation request to " + target.getName().getString() + "."), false);
                            target.sendMessage(Text.literal(requester.getName().getString() + " wants to teleport to you. Use /tpaccept to allow."), false);

                            return 1;
                        })));

        // Command to request another player to teleport to the requester.
        dispatcher.register(CommandManager.literal("tpahere")
                .requires(Permissions.require(TPAHERE_PERMISSION_NODE, 2))
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity requester = ctx.getSource().getPlayer();
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");

                            // Check if the target has teleport requests toggled off.
                            if (teleportToggleOff.contains(target.getUuid())) {
                                requester.sendMessage(Text.literal(target.getName().getString() + " has incoming teleport requests disabled."), false);
                                return 0;
                            }

                            // Check if requester is the same as target.
                            if (requester.equals(target)) {
                                requester.sendMessage(Text.literal("You can't send teleport requests to yourself!"), false);
                                return 0;
                            }

                            // Remove any existing requests from the requester to the target.
                            removeExistingRequestFrom(requester, target);

                            // Add the new request.
                            teleportRequests.computeIfAbsent(target, k -> new ArrayList<>())
                                    .add(new TeleportRequest(requester, TeleportRequest.RequestType.TPAHERE));

                            // Notify both players.
                            requester.sendMessage(Text.literal("You have sent a request for " + target.getName().getString() + " to teleport to you."), false);
                            target.sendMessage(Text.literal(requester.getName().getString() + " wants you to teleport to them. Use /tpaccept to allow."), false);

                            return 1;
                        })));

        // Command to accept a pending teleport request.
        dispatcher.register(CommandManager.literal("tpaccept")
                .executes(ctx -> {
                    ServerPlayerEntity target = ctx.getSource().getPlayer();

                    // If there's a request pending for the target player
                    if (teleportRequests.containsKey(target) && !teleportRequests.get(target).isEmpty()) {
                        TeleportRequest request = teleportRequests.get(target).remove(0); // Remove the oldest request
                        ServerPlayerEntity requester = request.requester;

                        // Depending on the type of request, perform the appropriate teleportation
                        if (request.type == TeleportRequest.RequestType.TPA) {
                            Location targetLocation = new Location(target.getServerWorld(), target.getX(), target.getY(), target.getZ());
                            targetLocation.teleport(requester);

                            requester.sendMessage(Text.literal("You have teleported to " + target.getName().getString() + "."), false);
                            target.sendMessage(Text.literal(requester.getName().getString() + " has teleported to you."), false);
                        } else {  // TeleportRequest.RequestType.TPAHERE
                            Location requesterLocation = new Location(requester.getServerWorld(), requester.getX(), requester.getY(), requester.getZ());
                            requesterLocation.teleport(target);

                            requester.sendMessage(Text.literal(target.getName().getString() + " has teleported to you."), false);
                            target.sendMessage(Text.literal("You have teleported to " + requester.getName().getString() + "."), false);
                        }

                        // If there are no more requests pending for the target, remove them from the map
                        if (teleportRequests.get(target).isEmpty()) {
                            teleportRequests.remove(target);
                        }
                    } else {
                        target.sendMessage(Text.literal("No teleportation requests pending."), false);
                    }

                    return 1;
                }));

        // Additional commands, such as /tpdeny, /tpacancel, and /tptoggle can be continued in a similar manner...

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

        // Register /tpno
        // Allows a player to deny a pending teleportation request.
        dispatcher.register(CommandManager.literal("tpno")
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

        // Register /tptoggle
        // Allows a player to toggle incoming teleportation requests.
        dispatcher.register(CommandManager.literal("tptoggle")
                .requires(Permissions.require(TPTOGGLE_PERMISSION_NODE, 2))
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    UUID playerId = player.getUuid();

                    if (teleportToggleOff.contains(playerId)) {
                        teleportToggleOff.remove(playerId);
                        player.sendMessage(Text.literal("Incoming teleport requests enabled."), false);
                    } else {
                        teleportToggleOff.add(playerId);
                        player.sendMessage(Text.literal("Incoming teleport requests disabled."), false);
                    }

                    return 1;
                }));
    }

    /**
     * Helper method to remove any existing teleport request from a given requester to a target.
     *
     * @param requester Player sending the request.
     * @param target    Player receiving the request.
     */
    private static void removeExistingRequestFrom(ServerPlayerEntity requester, ServerPlayerEntity target) {
        if (teleportRequests.containsKey(target)) {
            List<TeleportRequest> requests = teleportRequests.get(target);
            requests.removeIf(request -> request.requester.equals(requester));
            if (requests.isEmpty()) {
                teleportRequests.remove(target);
            }
        }
    }

    // Duration after which a teleport request is considered expired (2 minutes)
    private static final long TIMEOUT_DURATION = 2 * 60 * 1000;

    /**
     * Checks and removes expired teleport requests.
     */
    public static void checkForExpiredRequests() {
        long currentTimestamp = System.currentTimeMillis();
        List<ServerPlayerEntity> emptyKeys = new ArrayList<>();  // Players with no active requests

        teleportRequests.forEach((target, requests) -> {
            Iterator<TeleportRequest> iterator = requests.iterator();

            while (iterator.hasNext()) {
                TeleportRequest request = iterator.next();

                // If request has expired
                if ((currentTimestamp - request.timestamp) > TIMEOUT_DURATION) {
                    request.requester.sendMessage(Text.literal("Your teleport request to " + target.getName().getString() + " has expired."), false);
                    target.sendMessage(Text.literal(request.requester.getName().getString() + "'s teleport request has expired."), false);
                    iterator.remove();
                }
            }

            if (requests.isEmpty()) {
                emptyKeys.add(target);
            }
        });

        // Clean up the map by removing players with no active requests
        for (ServerPlayerEntity key : emptyKeys) {
            teleportRequests.remove(key);
        }
    }

}
package EEssentials.commands.teleportation;

import EEssentials.commands.AliasedCommand;
import EEssentials.lang.LangManager;
import EEssentials.settings.randomteleport.RTPSettings;
import EEssentials.settings.randomteleport.RTPWorldSettings;
import EEssentials.util.AsynchronousUtil;
import EEssentials.util.Location;
import EEssentials.util.RandomHelper;
import EEssentials.util.TeleportUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.literal;

public class RTPCommand {
    public static final String RTP_PERMISSION_NODE = "eessentials.rtp";
    public static final String RTP_COOLDOWN_BYPASS_PERMISSION_NODE = "eessentials.rtp.bypasscooldown";

    public static final List<String> queuedPlayerNames = new ArrayList<>();

    /**
     * Registers home rtp commands.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        new AliasedCommand() {
            @Override
            public LiteralCommandNode<ServerCommandSource> register(CommandDispatcher<ServerCommandSource> dispatcher) {
                return dispatcher.register(literal("randomteleport")
                        .requires(source -> Permissions.check(source, RTP_PERMISSION_NODE, 0))
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if(player != null) {
                                RTPWorldSettings worldSettings = RTPSettings.getWorldSettings(player.getServerWorld());
                                if(worldSettings != null) {
                                    long playerCooldown = worldSettings.getPlayerCooldown(player);
                                    if(playerCooldown <= 0 || Permissions.check(player, RTP_COOLDOWN_BYPASS_PERMISSION_NODE, 4)) {
                                        if(!queuedPlayerNames.contains(player.getName().getString())) {
                                            LangManager.send(context.getSource(), "RTP-Queued-Message");
                                            queuedPlayerNames.add(player.getName().getString());
                                            CompletableFuture<Void> rtp = teleportToRandomLocation(player, worldSettings);
                                            rtp.whenComplete((location, throwable) ->
                                                    queuedPlayerNames.remove(player.getName().getString()));
                                        } else {
                                            LangManager.send(context.getSource(), "RTP-Already-Queued-Message");
                                        }
                                    } else {
                                        Map<String, String> replacements = new HashMap<>();
                                        replacements.put("{cooldown}", String.valueOf(playerCooldown));
                                        LangManager.send(context.getSource(), "RTP-Cooldown-Message", replacements);
                                    }
                                } else {
                                    LangManager.send(context.getSource(), "RTP-World-Blacklisted");
                                }
                            } else {
                                LangManager.send(context.getSource(), "Invalid-Player-Only");
                            }
                            return Command.SINGLE_SUCCESS;
                        }));
            }

            @Override
            public String[] getCommandAliases() {
                return new String[]{"rtp"};
            }
        }.registerWithAliases(dispatcher);
    }

    private static CompletableFuture<Void> teleportToRandomLocation(ServerPlayerEntity player, RTPWorldSettings worldSettings) {
        return AsynchronousUtil.runTaskAsynchronously(() -> {
            Location location = getRandomLocation(worldSettings);
            Map<String, String> replacements = new HashMap<>();
            if(!player.isDisconnected()) {
                if (location != null) {
                    location.addReplacements(replacements);
                    location.teleport(player);
                    LangManager.send(player, "RTP-Success-Message", replacements);
                    worldSettings.startPlayerCooldown(player);
                } else {
                    replacements.put("{attempts}", String.valueOf(RTPSettings.getMaxAttempts()));
                    LangManager.send(player, "RTP-Location-Not-Found", replacements);
                }
            }
            return null;
        });
    }

    private static Location getRandomLocation(RTPWorldSettings settings) {
        ServerWorld world = settings.getWorld();
        if(world == null) return null;
        for(int i = 0; i< RTPSettings.getMaxAttempts(); i++) {
            int x = settings.getRandomIntInBounds();
            int y;
            int z = settings.getRandomIntInBounds();
            if(settings.allowCaveTeleports()) {
                y = (int) TeleportUtil.findNextBelow(world, x, RandomHelper.randomIntBetween(-64, settings.getHighestY()), z);
            } else {
                y = (int) TeleportUtil.findNextBelowNoCaves(world, x, settings.getHighestY(), z);
            }
            if(y != -1000) {
                Optional<RegistryKey<Biome>> optionalBiome = world.getBiome(new BlockPos(x,y,z)).getKey();
                if(optionalBiome.isPresent()) {
                    String biomeKey = optionalBiome.get().getValue().toString();
                    if (!RTPSettings.isBiomeBlacklisted(biomeKey)) {
                        return new Location(world, x, y, z);
                    }
                }
            }
        }
        return null;
    }
}

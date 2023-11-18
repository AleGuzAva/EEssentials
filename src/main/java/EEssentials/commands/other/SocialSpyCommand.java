package EEssentials.commands.other;

import EEssentials.lang.LangManager;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;

public class SocialSpyCommand {

    // Permission node for the Social Spy command.
    public static final String SOCIALSPY_PERMISSION_NODE = "eessentials.socialspy";

    private static final Set<ServerPlayerEntity> socialSpyEnabledPlayers = new HashSet<>();

    /**
     * Registers the social spy command.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("socialspy")
                .requires(Permissions.require(SOCIALSPY_PERMISSION_NODE, 2))
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    toggleSocialSpyFor(player);
                    if (isSocialSpyEnabled(player)) {
                        LangManager.send(player, "SocialSpy-Enabled");
                    } else {
                        LangManager.send(player, "SocialSpy-Disabled");
                    }
                    return 1;
                }));
    }

    private static void toggleSocialSpyFor(ServerPlayerEntity player) {
        if (isSocialSpyEnabled(player)) {
            socialSpyEnabledPlayers.remove(player);
        } else {
            socialSpyEnabledPlayers.add(player);
        }
    }

    public static boolean isSocialSpyEnabled(ServerPlayerEntity player) {
        return socialSpyEnabledPlayers.contains(player);
    }
}

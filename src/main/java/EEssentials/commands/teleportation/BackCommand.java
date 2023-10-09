package EEssentials.commands.teleportation;

import EEssentials.EEssentials;
import EEssentials.util.Location;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import static net.minecraft.server.command.CommandManager.*;

public class BackCommand {
    public static final String BACK_SELF_PERMISSION_NODE = "eessentials.back.self";
    public static final String BACK_OTHER_PERMISSION_NODE = "eessentials.back.other";

    /**
     * Registers the /back command to teleport the player to their last known location.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("back")
                        .requires(Permissions.require(BACK_SELF_PERMISSION_NODE, 2))
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            return teleportBack(player);
                        })
                        .then(argument("target", EntityArgumentType.player())
                                .requires(Permissions.require(BACK_OTHER_PERMISSION_NODE, 2))
                                .executes(ctx -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                                    return teleportBack(target);
                                }))
        );
    }

    private static int teleportBack(ServerPlayerEntity player) {
        Location backLocation = EEssentials.storage.getPlayerStorage(player).getPreviousLocation();
        if (backLocation != null) {
            backLocation.teleport(player);
            player.sendMessage(Text.literal("Teleported back to your last location."), false);
            return 1;
        } else {
            player.sendMessage(Text.literal("No last location found."), false);
            return 0;
        }
    }

}

package EEssentials.commands.teleportation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.block.BlockState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.minecraft.server.command.CommandManager.literal;

public class TopCommand {

    public static final String TOP_PERMISSION_NODE = "eessentials.top";

    /**
     * Registers the /top command.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("top")
                        .requires(Permissions.require(TOP_PERMISSION_NODE, 2))
                        .executes(TopCommand::teleportToTop)
        );
    }

    private static int teleportToTop(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        World world = player.getEntityWorld();
        BlockPos currentPosition = player.getBlockPos();

        // Find the topmost block from the world height downwards
        for (int y = world.getTopY() - 1; y > 0; y--) {
            BlockPos pos = new BlockPos(currentPosition.getX(), y, currentPosition.getZ());
            if (canPlayerStand(world, pos) &&
                    world.getBlockState(pos.up()).isAir() &&
                    world.getBlockState(pos.up(2)).isAir()) {
                player.teleport(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                player.sendMessage(Text.literal("Teleported to the top!").formatted(Formatting.GREEN), false);
                return 1;
            }
        }

        // If no suitable position is found, send an error message
        player.sendMessage(Text.of("Could not find a safe spot above you."), false);
        return 0;
    }

    private static boolean canPlayerStand(World world, BlockPos pos) {
        BlockState stateBelow = world.getBlockState(pos.down());
        BlockState stateCurrent = world.getBlockState(pos);
        BlockState stateAbove = world.getBlockState(pos.up());

        // Check if the current block space and the block space above it are non-solid or empty.
        // Also make sure that the block below is not air.
        return !stateBelow.isAir() &&
                (stateCurrent.isAir() || !stateCurrent.isFullCube(world, pos)) &&
                (stateAbove.isAir() || !stateAbove.isFullCube(world, pos.up()));
    }

}

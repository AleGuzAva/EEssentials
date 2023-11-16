package EEssentials.util;

import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class TeleportUtil {
    private static List<String> unsafeBlocks;
    private static List<String> airBlocks;

    public static void setAirBlocks(List<String> airBlocks) {
        TeleportUtil.airBlocks = airBlocks;
    }

    public static void setUnsafeBlocks(List<String> unsafeBlocks) {
        TeleportUtil.unsafeBlocks = unsafeBlocks;
    }

    private static boolean isBlockSafe(String blockID) {
        return !unsafeBlocks.contains(blockID);
    }

    public static double findNextAbove(ServerWorld world, double x, double y, double z) {
        BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
        if(y > 320) return -1000D;
        boolean foundBlock = false, air1 = false;
        for(int loopY = pos.getY(); loopY < 320; loopY++) {
            String blockID = Registries.BLOCK.getId(world.getBlockState(new BlockPos((int) x, loopY, (int) z)).getBlock()).toString();
            if(foundBlock && air1 && airBlocks.contains(blockID)) {
                return (loopY-pos.getY())+y-1;
            } else if(!airBlocks.contains(blockID)) {
                foundBlock = isBlockSafe(blockID);
                air1 = false;
            } else if(foundBlock) {
                air1 = true;
            }
        }
        return -1000D;
    }

    public static double findNextBelow(ServerWorld world, double x, double y, double z) {
        BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
        if(y < -64) return -1000D;
        boolean air1 = false, air2 = false;
        for(int loopY = pos.getY(); loopY > -64; loopY--) {
            String blockID = Registries.BLOCK.getId(world.getBlockState(new BlockPos((int) x, loopY, (int) z)).getBlock()).toString();
            if(airBlocks.contains(blockID)) {
                if(air1 && !air2) air2 = true;
                air1 = true;
            } else if(air1 && air2 && isBlockSafe(blockID)) {
                return (loopY-pos.getY())+y+1;
            } else {
                air1 = false;
                air2 = false;
            }
        }
        return -1000D;
    }

    public static double findNextBelowNoCaves(ServerWorld world, double x, double y, double z) {
        String blockID;
        double loopY = y;
        do {
            blockID = Registries.BLOCK.getId(world.getBlockState(new BlockPos((int) x, (int) loopY, (int) z)).getBlock()).toString();
            loopY--;
        } while (airBlocks.contains(blockID) && loopY > -64);
        if (isBlockSafe(blockID)) {
            return loopY + 2;
        } else return -1000D;
    }
}

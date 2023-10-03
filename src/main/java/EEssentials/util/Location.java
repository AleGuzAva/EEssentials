/**
 * Represents a specific location in the Minecraft world, including world, coordinates, and optional orientation.
 * Inspired by NeoAPI.
 */
package EEssentials.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class Location {
    private final ServerWorld world;  // The world where the location resides
    private final double x;           // The X-coordinate of the location
    private final double y;           // The Y-coordinate of the location
    private final double z;           // The Z-coordinate of the location
    private float pitch = -1000;      // The pitch (vertical orientation) at the location, default unset value is -1000
    private float yaw = -1000;        // The yaw (horizontal orientation) at the location, default unset value is -1000

    /**
     * Constructs a Location with given world and coordinates.
     */
    public Location(ServerWorld world, double x, double y, double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Constructs a Location with given world, coordinates, and orientation.
     */
    public Location(ServerWorld world, double x, double y, double z, float pitch, float yaw) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public static Location fromPlayer(ServerPlayerEntity player) {
        return new Location(player.getServerWorld(), player.getX(), player.getY(), player.getZ());
    }

    // Basic getter methods below...

    public ServerWorld getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    /**
     * Teleports a player to this location.
     * If yaw or pitch is unset (i.e., -1000), it retains the player's current orientation.
     */
    public void teleport(ServerPlayerEntity player) {
        float tpPitch = (pitch != -1000) ? pitch : player.getPitch();
        float tpYaw = (yaw != -1000) ? yaw : player.getYaw();
        player.teleport(world, x, y, z, tpYaw, tpPitch);
    }

    /**
     * Checks if this location is equal to another in terms of world, coordinates, and orientation.
     */
    public boolean isEqualTo(Location location) {
        return (world.equals(location.getWorld()))
                && (x == location.getX())
                && (y == location.getY())
                && (z == location.getZ())
                && (yaw == location.getYaw())
                && (pitch == location.getPitch());
    }

    /**
     * Checks if this location has the same coordinates (and world) as another, but ignoring pitch and yaw.
     */
    public boolean isEqualToCoordinatesOf(Location location) {
        return (world.equals(location.getWorld()))
                && (x == location.getX())
                && (y == location.getY())
                && (z == location.getZ());
    }
}


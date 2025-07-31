package com.github.e2318501.shigenUI;

import org.bukkit.Location;
import org.bukkit.World;

public record Coordinate(double x, double y, double z, float yaw, float pitch) {
    public static Coordinate fromLocation(Location location) {
        return new Coordinate(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }
}

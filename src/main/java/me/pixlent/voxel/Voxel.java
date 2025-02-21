package me.pixlent.voxel;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.Contract;

public record Voxel(int x, int y, int z) {

    public static Voxel from(Point point) {
        return new Voxel(point.blockX(), point.blockY(), point.blockZ());
    }

    @Contract(pure = true)
    public Voxel add(int x, int y, int z) {
        return new Voxel(this.x + x, this.y + y, this.z + z);
    }

    @Contract(pure = true)
    public Voxel add(int value) {
        return new Voxel(this.x + value, this.y + value, this.z + value);
    }

    @Contract(pure = true)
    public Voxel withX(int x) {
        return new Voxel(x, this.y, this.z);
    }

    @Contract(pure = true)
    public Voxel withY(int y) {
        return new Voxel(this.x, y, this.z);
    }

    @Contract(pure = true)
    public Voxel withZ(int z) {
        return new Voxel(this.x, this.y, z);
    }

    @Contract(pure = true)
    public Point asPoint() {
        return new Vec(x, y, z);
    }
}

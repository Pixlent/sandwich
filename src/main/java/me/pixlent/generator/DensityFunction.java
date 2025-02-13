package me.pixlent.generator;

import net.minestom.server.coordinate.Point;

public interface DensityFunction {
    float apply(int x, int y, int z);

    default float apply(Point pos) {
        return apply(pos.blockX(), pos.blockY(), pos.blockZ());
    }
}

package me.pixlent;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

public abstract class TerrainDecorator {
    abstract Block getBlock(Point pos, double density);
    //abstract void surfaceEvent(GenerationUnit unit, double[][][] cache, double density);
}

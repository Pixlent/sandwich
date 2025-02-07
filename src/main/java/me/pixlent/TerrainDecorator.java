package me.pixlent;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;

public abstract class TerrainDecorator {
    abstract Block getBlock(Vec pos, double density);
}

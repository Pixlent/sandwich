package me.pixlent.generator;

import me.pixlent.Generation;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

public interface TerrainDecorator {
    Block getBlock(Point localPos, Point worldPos, Generation generation);
}

package me.pixlent;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExampleTerrainDecorator extends TerrainDecorator {
    Random random = new Random(0);
    Block[] blocks;

    ExampleTerrainDecorator() {
        List<Block> blockList = new ArrayList<>();

        Block.values().forEach(block -> {
            if (block.isSolid() && !block.registry().isBlockEntity()) {
                blockList.add(block);
            }
        });

        blocks = blockList.toArray(new Block[0]);
    }

    @Override
    Block getBlock(Point pos, double density) {
        Block block = Block.AIR;

        if (pos.blockY() < 64) block = Block.WATER;
        if (density > 0.5) block = Block.STONE;

        return block;
    }

    Block pickRandomBlock() {
        return blocks[random.nextInt(blocks.length)];
    }
}

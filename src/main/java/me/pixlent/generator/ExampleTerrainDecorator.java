package me.pixlent.generator;

import me.pixlent.Generation;
import me.pixlent.SlopeBlock;
import me.pixlent.noise.NoiseRegistry;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;

import java.util.List;

public class ExampleTerrainDecorator implements TerrainDecorator {
    private static final List<SlopeBlock> SURFACE_SLOPE_BLOCKS = List.of(
            new SlopeBlock(20, Block.MOSS_BLOCK),
            new SlopeBlock(75, Block.GRASS_BLOCK),
            new SlopeBlock(80, Block.COBBLESTONE),
            new SlopeBlock(85, Block.STONE)
    );
    private static final List<SlopeBlock> WATER_SLOPE_BLOCKS = List.of(
            new SlopeBlock(45, Block.GRAVEL),
            new SlopeBlock(75, Block.STONE),
            new SlopeBlock(Double.MAX_VALUE, Block.STONE)
    );
    private static final List<SlopeBlock> BEACH_SLOPE_BLOCKS = List.of(
            new SlopeBlock(60, Block.SAND),
            new SlopeBlock(120, Block.SMOOTH_SANDSTONE),
            new SlopeBlock(Double.MAX_VALUE, Block.SAND)
    );

    @Override
    public Block getBlock(Point local, Point world, Generation generation) {
        Block block = Block.AIR;

        float density = generation.getDensity(local);

        final double beach_transition = 65 - (NoiseRegistry.BEACH_TRANSITION.evaluateNoise(world.blockX(), world.blockZ()) * 2);

        if (world.blockY() < 64) block = Block.WATER;
        if (density > 0) block = Block.STONE;

        if (generation.getDensity(local.add(0, 1, 0)) < 0 && block.equals(Block.STONE)) {
            final double slope = calculateSlope(generation, local, world);
            if (world.blockY() >= beach_transition) {
                for (final SlopeBlock slopeBlock : SURFACE_SLOPE_BLOCKS) {
                    if (slope <= slopeBlock.slopeDegree()) {
                        block = slopeBlock.blockType();
                        break;
                    }
                }
            } else if(world.blockY() > 60 - (NoiseRegistry.OCEAN_TRANSITION.evaluateNoise(world.blockX(), world.blockZ()) * 2)) {
                for (final SlopeBlock slopeBlock : BEACH_SLOPE_BLOCKS) {
                    if (slope <= slopeBlock.slopeDegree()) {
                        block = slopeBlock.blockType();
                        break;
                    }
                }
            } else {
                for (final SlopeBlock slopeBlock : WATER_SLOPE_BLOCKS) {
                    if (slope <= slopeBlock.slopeDegree()) {
                        block = slopeBlock.blockType();
                        break;
                    }
                }
            }
        }
        if (generation.getDensity(local.add(0, -1, 0)) > 0 && block.equals(Block.AIR) && local.blockY() - 66 > beach_transition) {
            if (NoiseRegistry.GRASS.evaluateNoise(world.blockX(), world.blockZ()) > 0.8
                    || NoiseRegistry.RANDOM.evaluateNoise(world.blockX(), world.blockZ()) > 0.1) {
                if (NoiseRegistry.RANDOM.evaluateNoise(world.blockX(), world.blockZ()) < 0.2) {
                    block = Block.TALL_GRASS.withProperty("half", "lower");
                    generation.unit.modifier().setBlock(world.add(0, 1, 0), Block.TALL_GRASS.withProperty("half", "upper"));
                } else block = Block.SHORT_GRASS;
            }
            if (NoiseRegistry.FLOWERS.evaluateNoise(world.blockX(), world.blockZ()) > .6 && NoiseRegistry.RANDOM.evaluateNoise(world.blockX(), world.blockZ()) > .6) {
                block = Block.POPPY;
            }
            if (NoiseRegistry.FLOWERS.evaluateNoise(world.blockX(), world.blockZ()) < -0.6 && NoiseRegistry.RANDOM.evaluateNoise(world.blockX(), world.blockZ()) > .6) {
                block = Block.DANDELION;
            }
            if (NoiseRegistry.RANDOM.evaluateNoise(world.blockX(), world.blockZ()) < -0.98) {
                placeTree(generation.unit, world);
            }
        }

        return block;
    }

    private double calculateSlope(final Generation generation, Point local, Point world) {
        final int radius = 1;
        final double threshold = 0; // Density threshold for solid blocks

        double maxDiff = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    double centerDensity = generation.getDensity(local);
                    double neighborDensity = generation.getSafeDensity(local.add(dx, dy, dz), world.add(dx, dy, dz));

                    // Calculate the difference in density
                    double diff = Math.abs(centerDensity - neighborDensity);

                    // If the densities are on opposite sides of the threshold, calculate the approximate height difference
                    if ((centerDensity - threshold) * (neighborDensity - threshold) < 0) {
                        double heightDiff = Math.abs(dy + (threshold - centerDensity) / (neighborDensity - centerDensity));
                        diff = Math.max(diff, heightDiff);
                    }

                    maxDiff = Math.max(maxDiff, diff);
                }
            }
        }

        return Math.toDegrees(Math.atan(maxDiff / radius));
    }

    private void placeTree(GenerationUnit unit, Point pos) {
        pos = pos.withY(pos.y() + getTreeHeight(pos));

        GenerationUnit fork = unit.fork(pos.add(-2, 0, -2), pos.add(3, 11, 3));
        fork.modifier().fill(pos.add(-2, 3, -2), pos.add(3, 5, 3), Block.OAK_LEAVES);
        fork.modifier().fill(pos.add(-1, 5, -1), pos.add(2, 7, 2), Block.OAK_LEAVES);

        placeLeaf(fork, pos.add(-2, 4, -2));
        placeLeaf(fork, pos.add(2, 4, -2));
        placeLeaf(fork, pos.add(-2, 4, 2));
        placeLeaf(fork, pos.add(2, 4, 2));

        placeLeaf(fork, pos.add(-1, 5, -1));
        placeLeaf(fork, pos.add(1, 5, -1));
        placeLeaf(fork, pos.add(-1, 5, 1));
        placeLeaf(fork, pos.add(1, 5, 1));

        placeLeaf(fork, pos.add(-1, 6, -1));
        placeLeaf(fork, pos.add(1, 6, -1));
        placeLeaf(fork, pos.add(-1, 6, 1));
        placeLeaf(fork, pos.add(1, 6, 1));

        fork.modifier().fill(pos.withY(pos.y() - getTreeHeight(pos)), pos.add(1, 6, 1), Block.OAK_WOOD);
    }

    private void placeLeaf(GenerationUnit fork, Point pos) {
        if (NoiseRegistry.RANDOM.evaluateNoise(pos.blockX(), pos.blockZ()) > 0.6) return;
        fork.modifier().setBlock(pos, Block.AIR);
    }

    private int getTreeHeight(Point pos) {
        double randomness = NoiseRegistry.RANDOM.evaluateNoise(pos.blockX(), pos.blockZ());
        if (randomness < .093) {
            return 0;
        }
        if (randomness > .096) {
            return -1;
        }
        return -2;
    }
}

package me.pixlent.voxel.procedural;

import me.pixlent.SlopeBlock;
import me.pixlent.voxel.GenericPixelChannel;
import me.pixlent.voxel.GenericVoxelChannel;
import me.pixlent.voxel.Voxel;
import me.pixlent.voxel.VoxelContext;
import me.pixlent.voxel.noise.NoisePixelChannel;
import me.pixlent.voxel.noise.NoiseVoxelChannel;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.UnitModifier;

import java.util.List;

public class ProcBlockPlacer implements Procedural {
    private final GenerationUnit unit;
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

    public ProcBlockPlacer(GenerationUnit unit) {
        this.unit = unit;
    }

    @Override
    public void apply(VoxelContext context) {
        NoiseVoxelChannel densityChannel = (NoiseVoxelChannel) context.getChannel("density", Float.class);
        if (densityChannel == null)
            throw new IllegalStateException("Channel '" + "density" + "' does not exist.");
        NoisePixelChannel whiteChannel = (NoisePixelChannel) context.getChannel("white", Float.class);
        if (whiteChannel == null)
            throw new IllegalStateException("Channel '" + "white" + "' does not exist.");
        NoisePixelChannel grassChannel = (NoisePixelChannel) context.getChannel("grass", Float.class);
        if (grassChannel == null)
            throw new IllegalStateException("Channel '" + "grass" + "' does not exist.");
        NoisePixelChannel flowerChannel = (NoisePixelChannel) context.getChannel("flowers", Float.class);
        if (flowerChannel == null)
            throw new IllegalStateException("Channel '" + "flowers" + "' does not exist.");
        NoisePixelChannel beachTransitionChannel = (NoisePixelChannel) context.getChannel("beach_transition", Float.class);
        if (beachTransitionChannel == null)
            throw new IllegalStateException("Channel '" + "beach_transition" + "' does not exist.");
        NoisePixelChannel oceanTransitionChannel = (NoisePixelChannel) context.getChannel("ocean_transition", Float.class);
        if (oceanTransitionChannel == null)
            throw new IllegalStateException("Channel '" + "ocean_transition" + "' does not exist.");

        for (int x = 0; x < context.size.x(); x++) {
            for (int z = 0; z < context.size.z(); z++) {
                for (int y = 0; y < context.size.y(); y++) {
                    Block block = Block.AIR;

                    float density = densityChannel.get(x, y, z);
                    if (!densityChannel.has(x, y, z)) break;

                    Voxel world = context.min.add(x, y, z);

                    if (world.y() < 64) block = Block.WATER;
                    if (density > 0f) {
                        block = Block.STONE;

                        float white = whiteChannel.get(x, z);

                        if (world.y() > 62) {
                            if (densityChannel.get(x, y + 4, z) < 0f && white > 0) block = Block.DIRT;
                            if (densityChannel.get(x, y + 3, z) < 0f) block = Block.DIRT;
                            if (densityChannel.get(x, y + 2, z) < 0f) block = Block.DIRT;
                        }

                        if (densityChannel.get(x, y + 1, z) < 0f) {
                            final double slope = calculateSlope(context, new Voxel(x, y, z), world);
                            if (world.y() >= 65 - beachTransitionChannel.get(x, z)) {
                                for (final SlopeBlock slopeBlock : SURFACE_SLOPE_BLOCKS) {
                                    if (slope <= slopeBlock.slopeDegree()) {
                                        block = slopeBlock.blockType();
                                        break;
                                    }
                                }
                            } else if(world.y() > 60 - oceanTransitionChannel.get(x, z)) {
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
                    }

                    if (y > 2) {
                        if (densityChannel.get(x, y - 2, z) > 0
                                && densityChannel.get(x, y - 1, z) < 0
                                && block.isAir()
                                && y - 67 > beachTransitionChannel.get(x, z)) {
                            if (grassChannel.get(x, z) > 0.8
                                    || whiteChannel.get(x, z) > 0.1) {
                                if (whiteChannel.get(x, z) < 0.2) {
                                    block = Block.TALL_GRASS.withProperty("half", "upper");
                                }
                            }
                        }

                        if (densityChannel.get(x, y - 1, z) > 0 && block.isAir() && y - 66 > beachTransitionChannel.get(x, z)) {
                            if (grassChannel.get(x, z) > 0.8
                                    || whiteChannel.get(x, z) > 0.1) {
                                if (whiteChannel.get(x, z) < 0.2) {
                                    block = Block.TALL_GRASS.withProperty("half", "lower");
                                } else block = Block.SHORT_GRASS;
                            }
                            if (flowerChannel.get(x, z) > .6 && whiteChannel.get(x, z) > .6) {
                                block = Block.POPPY;
                            }
                            if (flowerChannel.get(x, z) < -0.6 && whiteChannel.get(x, z) > .6) {
                                block = Block.DANDELION;
                            }
                            if (whiteChannel.get(x, z) < -0.98) {
                                placeTree(context, x, z, world);
                            }
                        }
                    }

                    //boolean isManual = (x % spacing == 0) && (y % spacing == 0) && (z % spacing == 0);

                    if (!block.isAir()) unit.modifier().setBlock(world.x(), world.y(), world.z(), block);
                }
            }
        }
    }

    private double calculateSlope(final VoxelContext context, Voxel local, Voxel world) {
        final int radius = 1;
        final double threshold = 0; // Density threshold for solid blocks

        NoiseVoxelChannel densityChannel = (NoiseVoxelChannel) context.getChannel("density", Float.class);

        double maxDiff = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    double centerDensity = densityChannel.get(local.x(), local.y(), local.z());
                    double neighborDensity = densityChannel.getSafe(local.x(), local.y(), local.z());

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

    private void placeTree(VoxelContext context, int x, int z, Voxel world) {
        NoisePixelChannel whiteChannel = (NoisePixelChannel) context.getChannel("white", Float.class);

        Point pos = world.withY((int) (world.y() + whiteChannel.get(x, z))).asPoint();

        GenerationUnit fork = unit.fork(pos.add(-2, 0, -2), pos.add(3, 11, 3));
        fork.modifier().fill(pos.add(-2, 3, -2), pos.add(3, 5, 3), Block.OAK_LEAVES);
        fork.modifier().fill(pos.add(-1, 5, -1), pos.add(2, 7, 2), Block.OAK_LEAVES);

        placeLeaf(whiteChannel, fork, pos.add(-2, 4, -2), x, z);
        placeLeaf(whiteChannel, fork, pos.add(2, 4, -2), x, z);
        placeLeaf(whiteChannel, fork, pos.add(-2, 4, 2), x, z);
        placeLeaf(whiteChannel, fork, pos.add(2, 4, 2), x, z);

        placeLeaf(whiteChannel, fork, pos.add(-1, 5, -1), x, z);
        placeLeaf(whiteChannel, fork, pos.add(1, 5, -1), x, z);
        placeLeaf(whiteChannel, fork, pos.add(-1, 5, 1), x, z);
        placeLeaf(whiteChannel, fork, pos.add(1, 5, 1), x, z);

        placeLeaf(whiteChannel, fork, pos.add(-1, 6, -1), x, z);
        placeLeaf(whiteChannel, fork, pos.add(1, 6, -1), x, z);
        placeLeaf(whiteChannel, fork, pos.add(-1, 6, 1), x, z);
        placeLeaf(whiteChannel, fork, pos.add(1, 6, 1), x, z);

        fork.modifier().fill(pos.withY(pos.y() - whiteChannel.get(x, z)), pos.add(1, 6, 1), Block.OAK_WOOD);
    }

    private void placeLeaf(NoisePixelChannel whiteChannel, GenerationUnit fork, Point pos, int x, int z) {
        if (whiteChannel.get(x, z) > 0.6) return;
        fork.modifier().setBlock(pos, Block.AIR);
    }
}
package me.pixlent.voxel.procedural;

import me.pixlent.SlopeBlock;
import me.pixlent.voxel.Voxel;
import me.pixlent.voxel.VoxelContext;
import me.pixlent.voxel.noise.NoisePixelChannel;
import me.pixlent.voxel.noise.NoiseVoxelChannel;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;

import java.util.List;

public class ProcBlockPlacer implements Procedural {
    private final GenerationUnit unit;
    private static final List<SlopeBlock> SURFACE_SLOPE_BLOCKS = List.of(
            new SlopeBlock(30, Block.MOSS_BLOCK),
            new SlopeBlock(60, Block.GRASS_BLOCK),
            new SlopeBlock(100, Block.COBBLESTONE),
            new SlopeBlock(120, Block.STONE)
    );
    private static final List<SlopeBlock> WATER_SLOPE_BLOCKS = List.of(
            new SlopeBlock(20, Block.GRAVEL),
            new SlopeBlock(35, Block.STONE),
            new SlopeBlock(Double.MAX_VALUE, Block.STONE)
    );
    private static final List<SlopeBlock> BEACH_SLOPE_BLOCKS = List.of(
            new SlopeBlock(15, Block.SAND),
            new SlopeBlock(30, Block.SMOOTH_SANDSTONE),
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
                        block = Block.BEDROCK;

                        float white = whiteChannel.get(x, z);

                        if (world.y() > 62) {
                            if (densityChannel.get(x, y + 4, z) < 0f && white > 0) block = Block.DIRT;
                            if (densityChannel.get(x, y + 3, z) < 0f) block = Block.DIRT;
                            if (densityChannel.get(x, y + 2, z) < 0f) block = Block.DIRT;
                        }

                        if (densityChannel.get(x, y + 1, z) < 0f) {
                            final double slope = calculateSlope(context, new Voxel(x, y, z), world);
                            System.out.println(slope);
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

    private double calculateSlope(final VoxelContext context, final Voxel local, final Voxel world) {
        final NoiseVoxelChannel densityChannel = (NoiseVoxelChannel) context.getChannel("density", Float.class);

        final double wx = world.x();
        final double wy = world.y();
        final double wz = world.z();

        final double h = 1.0;

        final double px = sampleDensity(densityChannel, context, wx + h, wy, wz);
        final double nx = sampleDensity(densityChannel, context, wx - h, wy, wz);
        final double py = sampleDensity(densityChannel, context, wx, wy + h, wz);
        final double ny = sampleDensity(densityChannel, context, wx, wy - h, wz);
        final double pz = sampleDensity(densityChannel, context, wx, wy, wz + h);
        final double nz = sampleDensity(densityChannel, context, wx, wy, wz - h);

        final double gx = (px - nx) / (2.0 * h);
        final double gy = (py - ny) / (2.0 * h);
        final double gz = (pz - nz) / (2.0 * h);

        final double gradientLength = Math.sqrt(gx * gx + gy * gy + gz * gz);
        if (gradientLength < 1e-6) {
            return 0.0;
        }

        final double ny_norm = -gy / gradientLength;

        double dotProduct = ny_norm;
        dotProduct = Math.max(-1.0, Math.min(1.0, dotProduct));

        return Math.toDegrees(Math.acos(dotProduct));
    }

    private double sampleDensity(final NoiseVoxelChannel channel, final VoxelContext context, final double wx, final double wy, final double wz) {
        final int localX = (int)(wx - context.min.x());
        final int localY = (int)(wy - context.min.y());
        final int localZ = (int)(wz - context.min.z());

        final int x0 = Math.max(0, Math.min(context.size.x() - 1, localX));
        final int x1 = Math.max(0, Math.min(context.size.x() - 1, localX + 1));
        final int y0 = Math.max(0, Math.min(context.size.y() - 1, localY));
        final int y1 = Math.max(0, Math.min(context.size.y() - 1, localY + 1));
        final int z0 = Math.max(0, Math.min(context.size.z() - 1, localZ));
        final int z1 = Math.max(0, Math.min(context.size.z() - 1, localZ + 1));

        final double fx = localX - x0;
        final double fy = localY - y0;
        final double fz = localZ - z0;

        final double c000 = channel.get(x0, y0, z0);
        final double c001 = channel.get(x0, y0, z1);
        final double c010 = channel.get(x0, y1, z0);
        final double c011 = channel.get(x0, y1, z1);
        final double c100 = channel.get(x1, y0, z0);
        final double c101 = channel.get(x1, y0, z1);
        final double c110 = channel.get(x1, y1, z0);
        final double c111 = channel.get(x1, y1, z1);

        return trilinearInterpolate(
                c000, c001, c010, c011,
                c100, c101, c110, c111,
                fx, fy, fz
        );
    }

    private double trilinearInterpolate(
            final double c000, final double c001, final double c010, final double c011,
            final double c100, final double c101, final double c110, final double c111,
            final double x, final double y, final double z
    ) {
        final double c00 = c000 * (1 - x) + c100 * x;
        final double c01 = c001 * (1 - x) + c101 * x;
        final double c10 = c010 * (1 - x) + c110 * x;
        final double c11 = c011 * (1 - x) + c111 * x;

        final double c0 = c00 * (1 - y) + c10 * y;
        final double c1 = c01 * (1 - y) + c11 * y;

        return c0 * (1 - z) + c1 * z;
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
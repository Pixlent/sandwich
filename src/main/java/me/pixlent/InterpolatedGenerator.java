package me.pixlent;

import me.pixlent.noise.NoiseRegistry;
import me.pixlent.utils.TrilinearInterpolator;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InterpolatedGenerator implements Generator {
    final TerrainBuilder terrainBuilder;
    final TerrainDecorator terrainDecorator;

    private final TrilinearInterpolator trilinearInterpolator = new TrilinearInterpolator();

    public final int SAMPLE_INTERVAL = 4;

    public InterpolatedGenerator(TerrainBuilder terrainBuilder, TerrainDecorator terrainDecorator) {
        this.terrainBuilder = terrainBuilder;
        this.terrainDecorator = terrainDecorator;
    }

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
    public void generate(@NotNull GenerationUnit unit) {
        Point min = unit.absoluteStart();
        Point max = unit.absoluteEnd();
        Point size = unit.size();

        int CHUNK_SIZE = 16;

        double[][][] cache = new double[CHUNK_SIZE][max.blockY() - min.blockY() + 2][CHUNK_SIZE];

        for (int x = 0; x < size.blockX(); x += SAMPLE_INTERVAL) {
            for (int z = 0; z < size.blockZ(); z += SAMPLE_INTERVAL) {
                for (int y = size.blockY(); y > 0; y -= SAMPLE_INTERVAL) {
                    sampleCube(cache, min.add(x, y - 1, z), min, max);
                }
            }
        }

        for (int x = 0; x < size.blockX(); x++) {
            for (int z = 0; z < size.blockZ(); z++) {
                final double beach_transition = 65 - (NoiseRegistry.BEACH_TRANSITION.evaluateNoise(min.blockX() + x, min.blockZ() + z) * 2);
                for (int y = size.blockY(); y > 0; y--) {
                    Point pos = min.add(x, y - 1, z);
                    Block block = Block.AIR;

                    if (pos.blockY() < 64) block = Block.WATER;
                    if (cache[x][y][z] > 0.5) block = Block.STONE;
                    if (cache[x][y+1][z] < 0.5 && block.equals(Block.STONE)) {
                        final double slope = calculateSlope(cache, pos, min);
                        if (pos.y() >= beach_transition) {
                            for (final SlopeBlock slopeBlock : SURFACE_SLOPE_BLOCKS) {
                                if (slope <= slopeBlock.slopeDegree()) {
                                    block = slopeBlock.blockType();
                                    break;
                                }
                            }
                        } else if(pos.y() > 60 - (NoiseRegistry.OCEAN_TRANSITION.evaluateNoise(min.blockX() + x, min.blockZ() + z) * 2)) {
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
                    if (cache[x][y-1][z] > 0.5 && block.equals(Block.AIR) && y - 66 > beach_transition) {
                        if (NoiseRegistry.GRASS.evaluateNoise(pos.blockX(), pos.blockZ()) > 0.8
                                || NoiseRegistry.RANDOM.evaluateNoise(pos.blockX(), pos.blockZ()) > 0.1) {
                                if (NoiseRegistry.RANDOM.evaluateNoise(pos.blockX(), pos.blockZ()) < 0.2) {
                                    block = Block.TALL_GRASS.withProperty("half", "lower");
                                    unit.modifier().setBlock(pos.add(0, 1, 0), Block.TALL_GRASS.withProperty("half", "upper"));
                                } else block = Block.SHORT_GRASS;
                        }
                        if (NoiseRegistry.FLOWERS.evaluateNoise(pos.blockX(), pos.blockZ()) > .6 && NoiseRegistry.RANDOM.evaluateNoise(pos.blockX(), pos.blockZ()) > .6) {
                            block = Block.POPPY;
                        }
                        if (NoiseRegistry.FLOWERS.evaluateNoise(pos.blockX(), pos.blockZ()) < -0.6 && NoiseRegistry.RANDOM.evaluateNoise(pos.blockX(), pos.blockZ()) > .6) {
                            block = Block.DANDELION;
                        }
                        if (NoiseRegistry.RANDOM.evaluateNoise(pos.blockX(), pos.blockZ()) < -0.98) {
                            placeTree(unit, pos);
                        }
                    }

                    unit.modifier().setBlock(pos, block);
                }
            }
        }
    }

    private void sampleCube(double[][][] cache, Point pos, Point min, Point max) {
        double c000 = terrainBuilder.getDensity(pos.blockX(), pos.blockY(), pos.blockZ());
        double c001 = terrainBuilder.getDensity(pos.blockX(), pos.blockY(), pos.blockZ() + SAMPLE_INTERVAL);
        double c010 = terrainBuilder.getDensity(pos.blockX(), pos.blockY() + SAMPLE_INTERVAL, pos.blockZ());
        double c011 = terrainBuilder.getDensity(pos.blockX(), pos.blockY() + SAMPLE_INTERVAL, pos.blockZ() + SAMPLE_INTERVAL);
        double c100 = terrainBuilder.getDensity(pos.blockX() + SAMPLE_INTERVAL, pos.blockY(), pos.blockZ());
        double c101 = terrainBuilder.getDensity(pos.blockX() + SAMPLE_INTERVAL, pos.blockY(), pos.blockZ() + SAMPLE_INTERVAL);
        double c110 = terrainBuilder.getDensity(pos.blockX() + SAMPLE_INTERVAL, pos.blockY() + SAMPLE_INTERVAL, pos.blockZ());
        double c111 = terrainBuilder.getDensity(pos.blockX() + SAMPLE_INTERVAL, pos.blockY() + SAMPLE_INTERVAL, pos.blockZ() + SAMPLE_INTERVAL);

        // Interpolate between sampled points
        for (int dx = 0; dx < SAMPLE_INTERVAL && pos.blockX() + dx < max.blockX(); dx++) {
            for (int dz = 0; dz < SAMPLE_INTERVAL && pos.blockZ() + dz < max.blockZ(); dz++) {
                for (int dy = 0; dy < SAMPLE_INTERVAL && pos.blockY() + dy < max.blockY(); dy++) {
                    int cacheX = pos.blockX() - min.blockX() + dx;
                    int cacheY = pos.blockY() - min.blockY() + dy;
                    int cacheZ = pos.blockZ() - min.blockZ() + dz;

                    cache[cacheX][cacheY][cacheZ] = trilinearInterpolator.interpolate(
                            c000, c001, c010, c011, c100, c101, c110, c111,
                            (double) dx / SAMPLE_INTERVAL,
                            (double) dy / SAMPLE_INTERVAL,
                            (double) dz / SAMPLE_INTERVAL
                    );
                }
            }
        }
    }

    private double calculateSlope(final double[][][] cache, Point pos, Point min) {
        final int radius = 1;
        final double threshold = 0.5; // Density threshold for solid blocks

        double maxDiff = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    double centerDensity = cache[pos.blockX() - min.blockX()][pos.blockY() - min.blockY()][pos.blockZ() - min.blockZ()];
                    double neighborDensity = getDensity(cache, new Vec(pos.blockX() + dx, pos.blockY() + dy, pos.blockZ() + dz), min);

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

    private double getDensity(double[][][] cache, Point pos, Point min) {
        if (pos.blockX() - min.blockX() >= 0 && pos.blockX() - min.blockX() < cache.length &&
                pos.blockY() - min.blockY() >= 0 && pos.blockY() - min.blockY() < cache[0].length &&
                pos.blockZ() - min.blockZ() >= 0 && pos.blockZ() - min.blockZ() < cache[0][0].length)

            return cache[pos.blockX() - min.blockX()][pos.blockY() - min.blockY()][pos.blockZ() - min.blockZ()];
        else return terrainBuilder.getDensity(pos.blockX(), pos.blockY(), pos.blockZ());
    }
}
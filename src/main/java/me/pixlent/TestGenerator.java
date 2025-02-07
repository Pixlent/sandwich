package me.pixlent;

import me.pixlent.utils.ExecutionTimer;
import me.pixlent.utils.TrilinearInterpolator;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

public class TestGenerator implements Generator {
    final TerrainBuilder terrainBuilder;
    final TerrainDecorator terrainDecorator;

    public TestGenerator(TerrainBuilder terrainBuilder, TerrainDecorator terrainDecorator) {
        this.terrainBuilder = terrainBuilder;
        this.terrainDecorator = terrainDecorator;
    }

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        final ExecutionTimer timer = new ExecutionTimer();
        final Point min = unit.absoluteStart();
        final Point max = unit.absoluteEnd();

        generateChunk(unit);

        for (int x = min.blockX(); x < max.blockX(); x++) {
            for (int z = min.blockZ(); z < max.blockZ(); z++) {
                // Column & Rows
                //generate2D(min, max, x, z, unit);
            }
        }

        Main.addTime(timer.finished());
    }

    private void generate2D(Point min, Point max, int x, int z, GenerationUnit unit) {
        int height = terrainBuilder.getSurfaceHeight(x, z);
        //unit.modifier().fill(min.withX(x).withZ(z), new Vec(x + 1, 63, z + 1), Block.WATER);
        unit.modifier().fill(min.withX(x).withZ(z), new Vec(x + 1, height, z + 1), Block.STONE);
    }

    public void generateChunk(GenerationUnit unit) {
        final int WORLD_MAX_Y = 256;
        final int WORLD_MIN_Y = -64;
        final int SAMPLE_INTERVAL = 4;

        final TrilinearInterpolator trilinearInterpolator = new TrilinearInterpolator();

        final Point min = unit.absoluteStart();
        final Point max = unit.absoluteEnd();

        for (int x = min.blockX(); x < max.blockX(); x += SAMPLE_INTERVAL) {
            for (int z = min.blockZ(); z < max.blockZ(); z += SAMPLE_INTERVAL) {
                for (int y = WORLD_MAX_Y; y > WORLD_MIN_Y; y -= SAMPLE_INTERVAL) {
                    // Sample density values at larger intervals
                    double c000 = terrainBuilder.getDensity(x, y, z);
                    double c001 = terrainBuilder.getDensity(x, y, z + SAMPLE_INTERVAL);
                    double c010 = terrainBuilder.getDensity(x, y + SAMPLE_INTERVAL, z);
                    double c011 = terrainBuilder.getDensity(x, y + SAMPLE_INTERVAL, z + SAMPLE_INTERVAL);
                    double c100 = terrainBuilder.getDensity(x + SAMPLE_INTERVAL, y, z);
                    double c101 = terrainBuilder.getDensity(x + SAMPLE_INTERVAL, y, z + SAMPLE_INTERVAL);
                    double c110 = terrainBuilder.getDensity(x + SAMPLE_INTERVAL, y + SAMPLE_INTERVAL, z);
                    double c111 = terrainBuilder.getDensity(x + SAMPLE_INTERVAL, y + SAMPLE_INTERVAL, z + SAMPLE_INTERVAL);

                    // Interpolate between sampled points
                    for (int dx = 0; dx < SAMPLE_INTERVAL && x + dx < max.blockX(); dx++) {
                        for (int dz = 0; dz < SAMPLE_INTERVAL && z + dz < max.blockZ(); dz++) {
                            for (int dy = 0; dy < SAMPLE_INTERVAL && y - dy > WORLD_MIN_Y; dy++) {
                                double interpolatedDensity = trilinearInterpolator.interpolate(
                                        c000, c001, c010, c011, c100, c101, c110, c111,
                                        (double) dx / SAMPLE_INTERVAL,
                                        (double) dy / SAMPLE_INTERVAL,
                                        (double) dz / SAMPLE_INTERVAL
                                );

                                Vec pos = new Vec(x + dx, y + dy, z + dz);

                                unit.modifier().setBlock(pos,
                                        terrainDecorator.getBlock(pos, interpolatedDensity));
                            }
                        }
                    }
                }
            }
        }
    }
}
package me.pixlent;

import me.pixlent.utils.ExecutionTimer;
import me.pixlent.utils.TrilinearInterpolator;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

public class InterpolatedGenerator implements Generator {
    final TerrainBuilder terrainBuilder;
    final TerrainDecorator terrainDecorator;

    private GenerationUnit unit;
    private Point min;
    private Point max;

    public final int SAMPLE_INTERVAL = 4;
    public final int CACHE_SIZE = 2;

    private final int CHUNK_SIZE = 16;

    public InterpolatedGenerator(TerrainBuilder terrainBuilder, TerrainDecorator terrainDecorator) {
        this.terrainBuilder = terrainBuilder;
        this.terrainDecorator = terrainDecorator;
    }

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        this.unit = unit;
        final ExecutionTimer timer = new ExecutionTimer();

        min = unit.absoluteStart();
        max = unit.absoluteEnd();

        final TrilinearInterpolator trilinearInterpolator = new TrilinearInterpolator();
        int worldHeight = max.blockY() - min.blockY();
        DensityCache densityCache = new DensityCache(CACHE_SIZE, CHUNK_SIZE, worldHeight / SAMPLE_INTERVAL + 1);

        // Initial sampling for the first two x-coordinates
        for (int x = 0; x < CACHE_SIZE; x++) {
            sampleDensityStrip(min.blockX() + x, min.blockZ(), min.blockY(), max.blockY(), densityCache.getStrip(x));
        }

        // Generate terrain
        for (int worldX = min.blockX(); worldX < max.blockX(); worldX++) {
            updateDensityCache(worldX, densityCache);
            generateTerrainStrip(worldX, densityCache);
        }

        Main.addTime(timer.finished());
    }

    private void updateDensityCache(int worldX, DensityCache densityCache) {
        if (worldX + 1 < max.blockX()) {
            sampleDensityStrip(worldX + 1, min.blockZ(), min.blockY(), max.blockY(), densityCache.getNextStrip());
        }
    }

    private void generateTerrainStrip(int worldX, DensityCache densityCache) {
        for (int worldZ = min.blockZ(); worldZ < max.blockZ(); worldZ++) {
            generateTerrainColumn(worldX, worldZ, densityCache);
        }
    }

    private void generateTerrainColumn(int worldX, int worldZ, DensityCache densityCache) {
        for (int worldY = max.blockY() - 1; worldY >= min.blockY(); worldY--) {
            double density = interpolateDensity(worldX - min.blockX(), worldY - min.blockY(), worldZ - min.blockZ(),min.blockY(), max.blockY(), densityCache);
            Vec pos = new Vec(worldX - min.blockX(), worldY - min.blockY(), worldZ - min.blockZ());
            Block block = terrainDecorator.getBlock(new Vec(worldX, worldY, worldZ), density);
            unit.modifier().setBlock(pos, block);
        }
    }

    private void sampleDensityStrip(int worldX, int worldZ, int minY, int maxY, double[] strip) {
        for (int i = 0; i <= (maxY - minY) / SAMPLE_INTERVAL; i++) {
            int worldY = maxY - i * SAMPLE_INTERVAL;
            strip[i] = terrainBuilder.getDensity(worldX, worldY, worldZ);
        }
    }

    private double interpolateDensity(int localX, int localY, int localZ, int minY, int maxY, DensityCache densityCache) {
        int indexY = (maxY - (localY + minY)) / SAMPLE_INTERVAL;
        double t = (double)((maxY - (localY + minY)) % SAMPLE_INTERVAL) / SAMPLE_INTERVAL;

        double d00 = linearInterpolate(densityCache.getDensity(0, localZ, indexY), densityCache.getDensity(0, localZ, indexY + 1), t);
        double d10 = linearInterpolate(densityCache.getDensity(1, localZ, indexY), densityCache.getDensity(1, localZ, indexY + 1), t);

        return linearInterpolate(d00, d10, (double)(localX % SAMPLE_INTERVAL) / SAMPLE_INTERVAL);
    }

    private double linearInterpolate(double a, double b, double t) {
        return a * (1 - t) + b * t;
    }
}

class DensityCache {
    private final double[][][] cache;
    private int currentIndex = 0;

    public DensityCache(int cacheSize, int width, int height) {
        this.cache = new double[cacheSize][width][height];
    }

    public double[] getStrip(int index) {
        return cache[index][0];
    }

    public double[] getNextStrip() {
        currentIndex = (currentIndex + 1) % cache.length;
        return getStrip(currentIndex);
    }

    public double getDensity(int x, int z, int y) {
        return cache[(currentIndex + x) % cache.length][z][Math.min(y, cache[0][0].length - 1)];
    }
}
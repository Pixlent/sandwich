package me.pixlent.voxel.procedural;

import com.github.fastnoise.FastNoise;
import com.github.fastnoise.FloatArray;
import me.pixlent.voxel.PixelChannel;
import me.pixlent.voxel.Voxel;
import me.pixlent.voxel.VoxelChannel;
import me.pixlent.voxel.VoxelContext;

public class ProcNoise implements Procedural {
    private final String channelName;
    private final FastNoise fastNoise;
    private final int seed;
    private final float frequency;
    private final int spacing; // Resolution for interpolation
    private final boolean is3D;

    public ProcNoise(String channelName, FastNoise fastNoise, int seed, float frequency, int spacing, boolean is3D) {
        this.channelName = channelName;
        this.fastNoise = fastNoise;
        this.seed = seed;
        this.frequency = frequency;
        this.spacing = Math.max(1, spacing);
        this.is3D = is3D;
    }

    @Override
    public void apply(VoxelContext context) {
        if (is3D) {
            VoxelChannel<Float> channel = (VoxelChannel<Float>) context.getChannel(channelName, Float.class);
            if (channel == null) throw new IllegalStateException("VoxelChannel '" + channelName + "' does not exist.");
            apply3DNoise(channel, context);
        } else {
            PixelChannel<Float> channel = (PixelChannel<Float>) context.getChannel(channelName, Float.class);
            if (channel == null) throw new IllegalStateException("PixelChannel '" + channelName + "' does not exist.");
            apply2DNoise(channel, context);
        }
    }

    private void apply2DNoise(PixelChannel<Float> channel, VoxelContext context) {
        Voxel size = context.size;
        int width = size.x();
        int depth = size.z();

        float[][] noiseGrid = new float[width][depth];
        generateNoiseGrid2D(noiseGrid, context.min, width, depth);

        if (spacing > 1) {
            interpolateAndFill2D(channel, noiseGrid, width, depth);
        } else {
            fillWithoutInterpolation2D(channel, noiseGrid, width, depth);
        }
    }

    private void apply3DNoise(VoxelChannel<Float> channel, VoxelContext context) {
        Voxel size = context.size;
        int width = size.x();
        int height = size.y();
        int depth = size.z();

        float[][][] noiseGrid = new float[width][height][depth];
        generateNoiseGrid3D(noiseGrid, context.min, width, height, depth);

        if (spacing > 1) {
            interpolateAndFill3D(channel, noiseGrid, width, height, depth);
        } else {
            fillWithoutInterpolation3D(channel, noiseGrid, width, height, depth);
        }
    }

    private void generateNoiseGrid2D(float[][] noiseGrid, Voxel min, int width, int depth) {
        int totalSize = width * depth;
        FloatArray noiseOut = new FloatArray(totalSize);
        FloatArray xArray = new FloatArray(totalSize);
        FloatArray zArray = new FloatArray(totalSize);

        int index = 0;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                xArray.set(index, (min.x() + x * spacing) * frequency);
                zArray.set(index, (min.z() + z * spacing) * frequency);
                index++;
            }
        }

        fastNoise.genPositionArray2D(noiseOut, xArray, zArray, 0f, 0f, seed);

        index = 0;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                noiseGrid[x][z] = noiseOut.get(index++);
            }
        }
    }

    private void generateNoiseGrid3D(float[][][] noiseGrid, Voxel min, int gridWidth, int gridHeight, int gridDepth) {
        int totalSize = gridWidth * gridHeight * gridDepth;
        FloatArray noiseOut = new FloatArray(totalSize);
        FloatArray xArray = new FloatArray(totalSize);
        FloatArray yArray = new FloatArray(totalSize);
        FloatArray zArray = new FloatArray(totalSize);

        int index = 0;
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                for (int z = 0; z < gridDepth; z++) {
                    xArray.set(index, (min.x() + x * spacing) * frequency);
                    yArray.set(index, (min.y() + y * spacing) * frequency);
                    zArray.set(index, (min.z() + z * spacing) * frequency);
                    index++;
                }
            }
        }

        // Generate noise values
        fastNoise.genPositionArray3D(noiseOut, xArray, yArray, zArray, 0f, 0f, 0f, seed);

        // Store noise values in the grid
        index = 0;
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                for (int z = 0; z < gridDepth; z++) {
                    noiseGrid[x][y][z] = noiseOut.get(index++);
                }
            }
        }
    }

    private void fillWithoutInterpolation2D(PixelChannel<Float> channel, float[][] noiseGrid, int width, int depth) {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                channel.set(x, z, noiseGrid[x][z]);
            }
        }
    }

    private void fillWithoutInterpolation3D(VoxelChannel<Float> channel, float[][][] noiseGrid, int width, int height, int depth) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    channel.set(x, y, z, noiseGrid[x][y][z]);
                }
            }
        }
    }

    private void interpolateAndFill2D(PixelChannel<Float> channel, float[][] noiseGrid, int width, int depth) {
        for (int x = 0; x < width; x++) {
            float gx = (float) x / spacing;
            int x0 = (int) Math.floor(gx);
            int x1 = Math.min(x0 + 1, noiseGrid.length - 1);
            float tx = gx - x0;

            for (int z = 0; z < depth; z++) {
                float gz = (float) z / spacing;
                int z0 = (int) Math.floor(gz);
                int z1 = Math.min(z0 + 1, noiseGrid[0].length - 1);
                float tz = gz - z0;

                float c00 = noiseGrid[x0][z0];
                float c10 = noiseGrid[x1][z0];
                float c01 = noiseGrid[x0][z1];
                float c11 = noiseGrid[x1][z1];

                float interpolatedNoise = (c00 * (1 - tx) + c10 * tx) * (1 - tz) +
                        (c01 * (1 - tx) + c11 * tx) * tz;

                channel.set(x, z, interpolatedNoise);
            }
        }
    }

    private void interpolateAndFill3D(VoxelChannel<Float> channel, float[][][] noiseGrid, int width, int height, int depth) {
        for (int x = 0; x < width; x++) {
            float gx = (float) x / spacing;
            int x0 = (int) Math.floor(gx);
            int x1 = Math.min(x0 + 1, noiseGrid.length - 1);
            float tx = gx - x0;

            for (int y = 0; y < height; y++) {
                float gy = (float) y / spacing;
                int y0 = (int) Math.floor(gy);
                int y1 = Math.min(y0 + 1, noiseGrid[0].length - 1);
                float ty = gy - y0;

                for (int z = 0; z < depth; z++) {
                    float gz = (float) z / spacing;
                    int z0 = (int) Math.floor(gz);
                    int z1 = Math.min(z0 + 1, noiseGrid[0][0].length - 1);
                    float tz = gz - z0;

                    float interpolatedNoise = trilinearInterpolation(noiseGrid, x0, y0, z0, x1, y1, z1, tx, ty, tz);

                    channel.set(x, y, z, interpolatedNoise);
                }
            }
        }
    }

    private float trilinearInterpolation(float[][][] grid,
                                         int x0, int y0, int z0,
                                         int x1, int y1, int z1,
                                         float tx, float ty, float tz) {
        return (1 - tx) * (1 - ty) * (1 - tz) * grid[x0][y0][z0] +
                tx * (1 - ty) * (1 - tz) * grid[x1][y0][z0] +
                (1 - tx) * ty * (1 - tz) * grid[x0][y1][z0] +
                tx * ty * (1 - tz) * grid[x1][y1][z0] +
                (1 - tx) * (1 - ty) * tz * grid[x0][y0][z1] +
                tx * (1 - ty) * tz * grid[x1][y0][z1] +
                (1 - tx) * ty * tz * grid[x0][y1][z1] +
                tx * ty * tz * grid[x1][y1][z1];
    }
}

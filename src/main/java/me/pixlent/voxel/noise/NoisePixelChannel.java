package me.pixlent.voxel.noise;

import com.github.fastnoise.FastNoise;
import com.github.fastnoise.FloatArray;
import me.pixlent.voxel.PixelChannel;
import me.pixlent.voxel.Voxel;

import java.util.function.UnaryOperator;

public class NoisePixelChannel implements PixelChannel<Float>, NoiseCommon {
    private final float[][] data;
    private final Voxel min;
    private final int width, depth;

    private final FastNoise fastNoise;
    private final float frequency;
    private final int seed;
    private final int spacing;

    public NoisePixelChannel(Voxel min, int width, int depth, FastNoise fastNoise, int seed, float frequency, int spacing) {
        this.data = new float[width][depth];
        this.min = min;
        this.width = width;
        this.depth = depth;
        this.fastNoise = fastNoise;
        this.seed = seed;
        this.frequency = frequency;
        this.spacing = Math.max(1, spacing);

        if (spacing > 1) {
            // Generate sampled noise points
            int gridWidth = (int) Math.ceil((double) width / spacing) + 1;
            int gridDepth = (int) Math.ceil((double) depth / spacing) + 1;
            float[][] noiseGrid = new float[gridWidth][gridDepth];

            generateNoise(noiseGrid, gridWidth, gridDepth, spacing);
            interpolate(noiseGrid);
        } else {
            generateNoise(data, width, depth, 1); // No interpolation, fill directly
        }
    }

    private void generateNoise(float[][] targetGrid, int gridWidth, int gridDepth, int spacingFactor) {
        int totalSize = gridWidth * gridDepth;
        FloatArray noiseOut = new FloatArray(totalSize);
        FloatArray xArray = new FloatArray(totalSize);
        FloatArray zArray = new FloatArray(totalSize);

        int index = 0;
        for (int x = 0; x < gridWidth; x++) {
            for (int z = 0; z < gridDepth; z++) {
                xArray.set(index, (min.x() + x * spacingFactor) * frequency);
                zArray.set(index, (min.z() + z * spacingFactor) * frequency);
                index++;
            }
        }

        // Generate noise
        fastNoise.genPositionArray2D(noiseOut, xArray, zArray, 0f, 0f, seed);

        // Store noise in the target grid
        index = 0;
        for (int x = 0; x < gridWidth; x++) {
            for (int z = 0; z < gridDepth; z++) {
                targetGrid[x][z] = noiseOut.get(index++);
            }
        }
    }

    private void interpolate(float[][] noiseGrid) {
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

                data[x][z] = bilinearInterpolation(noiseGrid, x0, z0, x1, z1, tx, tz);
            }
        }
    }

    private float bilinearInterpolation(float[][] grid, int x0, int z0, int x1, int z1, float tx, float tz) {
        float c00 = grid[x0][z0];
        float c10 = grid[x1][z0];
        float c01 = grid[x0][z1];
        float c11 = grid[x1][z1];

        return (c00 * (1 - tx) + c10 * tx) * (1 - tz) +
                (c01 * (1 - tx) + c11 * tx) * tz;
    }

    @Override
    public Float get(int x, int z) {
        return data[x][z];
    }

    public float getSafe(int x, int z) {
        if (x >= 0 && x < data.length &&
                z >= 0 && z < data[0].length) {
            return data[x][z];
        } else {
            return fastNoise.genSingle2D(min.x() + x, min.z() + z, seed); // Generate noise if out of bounds
        }
    }

    @Override
    public boolean has(int x, int z) {
        return true;
    }

    @Override
    public void set(int x, int z, Float value) {
        data[x][z] = value;
    }

    @Override
    public void fill(Float value) {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                data[x][z] = value;
            }
        }
    }

    @Override
    public void apply(UnaryOperator<Float> operator) {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                data[x][z] = operator.apply(data[x][z]);
            }
        }
    }

    @Override
    public Voxel getMin() {
        return min;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public FastNoise getFastNoise() {
        return fastNoise;
    }

    @Override
    public float getFrequency() {
        return frequency;
    }

    @Override
    public int getSeed() {
        return seed;
    }

    @Override
    public int getSpacing() {
        return spacing;
    }
}

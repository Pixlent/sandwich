package me.pixlent.voxel.noise;

import com.github.fastnoise.FastNoise;
import com.github.fastnoise.FloatArray;
import me.pixlent.voxel.Voxel;
import me.pixlent.voxel.VoxelChannel;

import java.util.function.UnaryOperator;

public class NoiseVoxelChannel implements VoxelChannel<Float>, NoiseCommon {
    private final float[][][] data;
    private final Voxel min;
    private final int width, height, depth;

    private final FastNoise fastNoise;
    private final float frequency;
    private final int seed;
    private final int spacing;

    public NoiseVoxelChannel(Voxel min, int width, int height, int depth, FastNoise fastNoise, int seed, float frequency, int spacing) {
        this.data = new float[width][height][depth];
        this.min = min;
        this.width = width;
        this.depth = depth;
        this.height = height;
        this.fastNoise = fastNoise;
        this.seed = seed;
        this.frequency = frequency;
        this.spacing = Math.max(1, spacing);

        if (spacing > 1) {
            // Only generate sampled noise points
            int gridWidth = (int) Math.ceil((double) width / spacing) + 1;
            int gridHeight = (int) Math.ceil((double) height / spacing) + 1;
            int gridDepth = (int) Math.ceil((double) depth / spacing) + 1;
            float[][][] noiseGrid = new float[gridWidth][gridHeight][gridDepth];

            generateNoise(noiseGrid, gridWidth, gridHeight, gridDepth, spacing);
            interpolate(noiseGrid);
        } else {
            generateNoise(data, width, height, depth, 1); // No interpolation, fill directly
        }
    }

    private void generateNoise(float[][][] targetGrid, int gridWidth, int gridHeight, int gridDepth, int spacingFactor) {
        int totalSize = gridWidth * gridHeight * gridDepth;
        FloatArray noiseOut = new FloatArray(totalSize);
        FloatArray xArray = new FloatArray(totalSize);
        FloatArray yArray = new FloatArray(totalSize);
        FloatArray zArray = new FloatArray(totalSize);

        int index = 0;
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                for (int z = 0; z < gridDepth; z++) {
                    xArray.set(index, (min.x() + x * spacingFactor) * frequency);
                    yArray.set(index, (min.y() + y * spacingFactor) * frequency);
                    zArray.set(index, (min.z() + z * spacingFactor) * frequency);
                    index++;
                }
            }
        }

        // Generate noise
        fastNoise.genPositionArray3D(noiseOut, xArray, yArray, zArray, 0f, 0f, 0f, seed);

        // Store noise in the target grid
        index = 0;
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                for (int z = 0; z < gridDepth; z++) {
                    targetGrid[x][y][z] = noiseOut.get(index++);
                }
            }
        }
    }

    private void interpolate(float[][][] noiseGrid) {
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

                    data[x][y][z] = trilinearInterpolation(noiseGrid, x0, y0, z0, x1, y1, z1, tx, ty, tz);
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

    @Override
    public Float get(int x, int y, int z) {
        return data[x][y][z];
    }

    public float getSafe(int x, int y, int z) {
        if (x >= 0 && x < data.length &&
                y >= 0 && y < data[0].length &&
                z >= 0 && z < data[0][0].length) {
            return data[x][y][z];
        } else {
            return fastNoise.genSingle3D(min.x() + x, min.y() + y, min.z() + z, seed);
        }
    }

    @Override
    public boolean has(int x, int y, int z) {
        return true;
    }

    @Override
    public void set(int x, int y, int z, Float value) {
        data[x][y][z] = value;
    }

    @Override
    public void fill(Float value) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    data[x][y][z] = value;
                }
            }
        }
    }

    @Override
    public void apply(UnaryOperator<Float> operator) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    data[x][y][z] = operator.apply(data[x][y][z]);
                }
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
    public int getHeight() {
        return height;
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

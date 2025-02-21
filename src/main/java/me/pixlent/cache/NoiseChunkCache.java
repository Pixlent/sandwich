package me.pixlent.cache;

import me.pixlent.noise.NoiseFunction;
import me.pixlent.noise.modifier.FunctionModifier;
import net.minestom.server.coordinate.Point;

import java.util.Arrays;

public class NoiseChunkCache {
    private final NoiseFunction noiseFunction;
    private final float[][][] cache;
    private final float[][][] inputNoise;
    private final int inputSizeX, inputSizeY, inputSizeZ;
    private final int upscaleFactor;

    public NoiseChunkCache(final NoiseFunction noiseFunction, final Point world, NoiseInterpolationResolution resolution) {
        this.noiseFunction = noiseFunction;
        this.upscaleFactor = resolution.getUpscaleFactor();


        float[][][] inputNoise = new float[resolution.getWidth()][resolution.getHeight()][resolution.getWidth()];

        //noiseFunction.modifier(input -> input * 2 - 1);

        noiseFunction.apply(inputNoise,
                world.blockX(), world.blockY(), world.blockZ(),
                resolution.getWidth(), resolution.getHeight(), resolution.getWidth(),
                upscaleFactor, upscaleFactor, upscaleFactor
        );

        //System.out.println(Arrays.deepToString(inputNoise));

        this.inputNoise = inputNoise;
        this.inputSizeX = inputNoise.length;
        this.inputSizeY = inputNoise[0].length;
        this.inputSizeZ = inputNoise[0][0].length;

        int outputSizeX = inputSizeX * upscaleFactor;
        int outputSizeY = inputSizeY * upscaleFactor;
        int outputSizeZ = inputSizeZ * upscaleFactor;

        cache = new float[outputSizeX][outputSizeY][outputSizeZ];

        for (int x = 0; x < outputSizeX; x++) {
            for (int y = 0; y < outputSizeY; y++) {
                for (int z = 0; z < outputSizeZ; z++) {
                    cache[x][y][z] = getTrilinearNoise(x, y, z);
                }
            }
        }
    }

    /**
     * Returns interpolated noise using trilinear interpolation
     */
    private float getTrilinearNoise(float x, float y, float z) {
        float fx = x / upscaleFactor;
        float fy = y / upscaleFactor;
        float fz = z / upscaleFactor;

        int x0 = (int) fx;
        int y0 = (int) fy;
        int z0 = (int) fz;
        int x1 = Math.min(x0 + 1, inputSizeX - 1);
        int y1 = Math.min(y0 + 1, inputSizeY - 1);
        int z1 = Math.min(z0 + 1, inputSizeZ - 1);

        float tx = fx - x0;
        float ty = fy - y0;
        float tz = fz - z0;

        float c000 = inputNoise[x0][y0][z0];
        float c100 = inputNoise[x1][y0][z0];
        float c010 = inputNoise[x0][y1][z0];
        float c110 = inputNoise[x1][y1][z0];
        float c001 = inputNoise[x0][y0][z1];
        float c101 = inputNoise[x1][y0][z1];
        float c011 = inputNoise[x0][y1][z1];
        float c111 = inputNoise[x1][y1][z1];

        float i000 = lerp(c000, c100, tx);
        float i010 = lerp(c010, c110, tx);
        float i001 = lerp(c001, c101, tx);
        float i011 = lerp(c011, c111, tx);

        float i00 = lerp(i000, i010, ty);
        float i01 = lerp(i001, i011, ty);

        return lerp(i00, i01, tz);
    }

    /**
     * Linearly interpolates between two values
     */
    private float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    public float getNoise(int x, int y, int z) {
        return cache[x][y][z];
    }

    public float getNoise(Point pos) {
        return cache[pos.blockX()][pos.blockY()][pos.blockZ()];
    }

    public float getSafeNoise(Point local, Point world) {
        if (local.blockX() >= 0 && local.blockX() < cache.length &&
                local.blockY() >= 0 && local.blockY() < cache[0].length &&
                local.blockZ() >= 0 && local.blockZ() < cache[0][0].length) {
            return cache[local.blockX()][local.blockY()][local.blockZ()];
        } else {
            return noiseFunction.apply(world.blockX(), world.blockY(), world.blockZ());
        }
    }
}
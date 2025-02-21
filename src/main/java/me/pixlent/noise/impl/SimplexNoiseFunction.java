package me.pixlent.noise.impl;

import com.github.fastnoise.FastNoise;
import me.pixlent.noise.FastNoiseFunction;

public class SimplexNoiseFunction implements FastNoiseFunction {
    private final FastNoise fractal = new FastNoise("Simplex");
    private final int seed;
    private final float frequency;

    public SimplexNoiseFunction(int seed, float frequency) {
        this.seed = seed;
        this.frequency = frequency;
    }

    @Override
    public FastNoise getFastNoise() {
        return fractal;
    }

    @Override
    public int getSeed() {
        return seed;
    }

    @Override
    public float getFrequency() {
        return frequency;
    }
}

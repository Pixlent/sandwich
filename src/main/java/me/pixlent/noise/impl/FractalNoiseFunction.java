package me.pixlent.noise.impl;

import com.github.fastnoise.FastNoise;
import me.pixlent.noise.FastNoiseFunction;
import me.pixlent.noise.FastNoiseSource;

public class FractalNoiseFunction implements FastNoiseFunction {
    private final FastNoise fractal;
    private final int seed;
    private final float frequency;

    public FractalNoiseFunction(int seed, float frequency, FastNoise source, int octaves, float gain, float lacunarity) {
        this.seed = seed;
        this.frequency = frequency;

        fractal = new FastNoise("FractalFBm");
        fractal.set("Source", source);
        fractal.set("Octaves", octaves);
        fractal.set("Gain", gain);
        fractal.set("Lacunarity", lacunarity);
    }

    public FractalNoiseFunction(int seed, float frequency, FastNoiseSource source, int octaves, float gain, float lacunarity) {
        this(seed, frequency, source.getSource(), octaves, gain, lacunarity);
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

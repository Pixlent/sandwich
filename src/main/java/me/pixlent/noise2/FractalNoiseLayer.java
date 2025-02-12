package me.pixlent.noise2;

import com.github.fastnoise.FastNoise;

public class FractalNoiseLayer extends FastNoiseLayer {
    private final FastNoise fractal;

    public FractalNoiseLayer(int seed, float scale, FastNoise source, int octaves, float gain, float lacunarity) {
        super(seed, scale);

        fractal = new FastNoise("FractalFBm");
        fractal.set("Source", source);
        fractal.set("Octaves", octaves);
        fractal.set("Gain", gain);
        fractal.set("Lacunarity", lacunarity);
    }

    public FractalNoiseLayer(int seed, float scale, FastNoiseLayer source, int octaves, float gain, float lacunarity) {
        this(seed, scale, source.getFastNoise(), octaves, gain, lacunarity);
    }

    @Override
    FastNoise getFastNoise() {
        return fractal;
    }
}

package me.pixlent.noise2;

import com.github.fastnoise.FastNoise;

public class SimplexNoiseLayer extends FastNoiseLayer {
    private final FastNoise simplex = new FastNoise("Simplex");

    public SimplexNoiseLayer(int seed, float frequency) {
        super(seed, frequency);
    }

    @Override
    FastNoise getFastNoise() {
        return simplex;
    }
}

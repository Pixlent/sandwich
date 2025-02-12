package me.pixlent.noise2;

import com.github.fastnoise.FastNoise;

public abstract class FastNoiseLayer extends SeededNoiseLayer {
    private final float scale;

    public FastNoiseLayer(int seed, float scale) {
        super(seed);

        this.scale = scale;
    }

    abstract FastNoise getFastNoise();

    @Override
    public float sample(float x, float y, float z) {
        return getFastNoise().genSingle3D(x * scale, y * scale, z * scale, getSeed());
    }

    @Override
    public float sample(float x, float z) {
        return getFastNoise().genSingle2D(x * scale, z * scale, getSeed());
    }
}

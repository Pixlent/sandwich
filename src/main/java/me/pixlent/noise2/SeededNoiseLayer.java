package me.pixlent.noise2;

import lombok.Getter;

public abstract class SeededNoiseLayer extends NoiseLayer {
    @Getter private final int seed;

    protected SeededNoiseLayer(int seed) {
        this.seed = seed;
    }
}

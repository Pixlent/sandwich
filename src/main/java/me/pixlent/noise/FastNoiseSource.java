package me.pixlent.noise;

import com.github.fastnoise.FastNoise;
import lombok.Getter;

@Getter
@SuppressWarnings("all")
public enum FastNoiseSource {
    SIMPLEX(new FastNoise("Simplex")),
    FRACTAL(new FastNoise("FractalFBm")),
    WHITE(new FastNoise("White"));

    private final FastNoise source;

    FastNoiseSource(FastNoise source) {
        this.source = source;
    }
}

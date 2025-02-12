package me.pixlent.noise;

import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import de.articdive.jnoise.pipeline.JNoise;
import me.pixlent.TerrainGenerator;

import java.util.Objects;

public enum NoiseRegistry {
    WEIRDNESS(JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder()
                    .setSeed(hash("weirdness"))
                    .build())
            .octavate(5, 0.4, 1.2, FractalFunction.FBM, false)
            .scale(0.005)
            .addModifier(new NoiseUtils.AbsClampNoiseModifier())
            .build()),
    DENSITY(JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder()
                    .setSeed(hash("density"))
                    .build())
            .octavate(6, 0.6, 2, FractalFunction.FBM, false)
            .scale(0.00075)
            .addModifier(new TerrainGenerator.AbsClampNoiseModifier())
            .build()),
    CAVES(JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder()
                    .setSeed(hash("caves"))
                    .build())
            .octavate(6, 0.3, 1.8, FractalFunction.FBM, true)
            .scale(0.0111)
            .addModifier(new TerrainGenerator.AbsClampNoiseModifier())
            .build()),
    RANDOM(JNoise.newBuilder()
            .white(hash("random"))
            .build()),
    GRASS(JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder().setSeed(hash("grass")).build())
            .octavate(2, .3, 1.4, FractalFunction.FBM, false)
            .scale(.015)
            .build()),
    FLOWERS(JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder().setSeed(hash("flowers")).build())
            .octavate(2, .3, 1.4, FractalFunction.FBM, false)
            .scale(.04)
            .build()),
    BEACH_TRANSITION(JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder().setSeed(hash("beach_transition")).build())
            .octavate(2, .3, 1.4, FractalFunction.FBM, false)
            .scale(.02)
            .build()),
    OCEAN_TRANSITION(JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder().setSeed(hash("beach_transition")).build())
            .octavate(2, .3, 1.4, FractalFunction.FBM, false)
            .scale(.02)
            .build());

    final private JNoise noise;
    static final private long seed = 0;

    NoiseRegistry(JNoise noise) {
        this.noise = noise;
    }

    public double evaluateNoise(int x, int z) {
        return noise.evaluateNoise(x, z);
    }

    public double evaluateNoise(int x, int y, int z) {
        return noise.evaluateNoise(x, y, z);
    }

    private static long hash(Object... objects) {
        return Objects.hash(seed, Objects.hash(objects));
    }
}
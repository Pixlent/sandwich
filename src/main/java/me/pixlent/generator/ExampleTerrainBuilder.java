package me.pixlent.generator;

import me.pixlent.noise2.FractalNoiseLayer;
import me.pixlent.noise2.NoiseStack;
import me.pixlent.noise2.SimplexNoiseLayer;
import me.pixlent.noise2.modifier.AbsClampModifier;
import me.pixlent.noise2.modifier.SplineModifier;
import me.pixlent.utils.SplineInterpolator;

public class ExampleTerrainBuilder implements TerrainBuilder {
    private final int seed;
    private final NoiseStack density;
    private final NoiseStack continentalness;
    private final NoiseStack weirdness;
    private final SplineInterpolator heightBiasInterpolator = SplineInterpolator
            .builder()
            .add(0, 1)
            .add(64, 0.7)
            .add(128, 0)
            .add(384, -1)
            .build();

    public ExampleTerrainBuilder(int seed) {
        this.seed = seed;

        density = new NoiseStack();
        density.addLayer(new FractalNoiseLayer(0, 0.005f, new SimplexNoiseLayer(0, 0.005f), 6, 0.4f, 2.2f));

        continentalness = new NoiseStack();
        continentalness.addLayer(new FractalNoiseLayer(0, 0.0005f, new SimplexNoiseLayer(0, 0.25f), 5, 0.4f, 2.2f));
        continentalness.addModifier(new AbsClampModifier());
        continentalness.addModifier(new SplineModifier(SplineInterpolator.builder()
                .add(0.0, 0.0)
                .add(0.15, 0.1)
                .add(0.2, 0.3)
                .add(0.3, 0.5)
                .add(0.39, 0.6)
                .add(0.433, 0.777)
                .add(0.55, 0.888)
                .add(0.7, 0.97)
                .add(1, 1)
                .build()));

        weirdness = new NoiseStack();
        weirdness.addLayer(new SimplexNoiseLayer(1, 0.01f));
        weirdness.addModifier(new AbsClampModifier());
    }

    @Override
    public int getSeed() {
        return seed;
    }

    @Override
    public float getDensity(int x, int y, int z) {
        int surfaceHeight = Math.round(continentalness.sample(x, z) * 128);
        int offsetY = y - surfaceHeight + 100;

        float baseDensity = density.sample(x, offsetY, z);
        float heightBias = (float) heightBiasInterpolator.interpolate(offsetY + 64);

        return baseDensity + (heightBias * (weirdness.sample(x, z) + 1) * 1.8f);
    }
}

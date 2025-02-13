package me.pixlent.generator;

import me.pixlent.noise2.FractalNoiseLayer;
import me.pixlent.noise2.NoiseStack;
import me.pixlent.noise2.SimplexNoiseLayer;
import me.pixlent.noise2.combiner.LayerCombiner;
import me.pixlent.noise2.combiner.WeightCombiner;
import me.pixlent.noise2.modifier.AbsClampModifier;
import me.pixlent.noise2.modifier.SplineModifier;
import me.pixlent.utils.SplineInterpolator;

public class ExampleTerrainBuilder implements DensityFunction {
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

        density = new NoiseStack();
        density.addLayer(new FractalNoiseLayer(0, 0.007f, new SimplexNoiseLayer(0, Float.NaN), 4, 0.2f, 2.4f));
        density.addModifier(v -> v * 0.5f);

        continentalness = new NoiseStack();
        continentalness.addLayer(new FractalNoiseLayer(0, 0.0007f, new SimplexNoiseLayer(0, Float.NaN), 6, 0.6f, 2.3f));
        continentalness.addModifier(new AbsClampModifier());

        density.addLayer(new FractalNoiseLayer(0, 0.004f, new SimplexNoiseLayer(0, 0.05f), 5, 0.4f, 2.2f));

        continentalness = new NoiseStack();
        continentalness.addLayer(new FractalNoiseLayer(0, 0.001f, new SimplexNoiseLayer(0, 0.25f), 5, 0.5f, 2.4f));
        continentalness.addModifier(new SplineModifier(SplineInterpolator.builder()
                .add(-1.0, -1.0)
                .add(-0.7, -0.8)
                .add(-0.6, -0.4)
                .add(-0.4, 0)
                .add(-0.22, 0.2)
                .add(-0.134, 0.554)
                .add(0.1, 0.776)
                .add(0.4, 0.94)
                .add(1, 1)
                .build()));

        weirdness = new NoiseStack();
        weirdness.addLayer(new SimplexNoiseLayer(1, 0.005f));
        weirdness.addModifier(new AbsClampModifier());
    }

    @Override
    public float apply(int x, int y, int z) {
        int surfaceHeight = Math.round(continentalness.sample(x, z) * 128);
        int offsetY = y - surfaceHeight + 100;

        float baseDensity = density.sample(x, offsetY, z);
        float heightBias = (float) heightBiasInterpolator.interpolate(offsetY + 64);

        return baseDensity + (heightBias * (weirdness.sample(x, z) + 1) * 1.8f);
    }

    public int getSeed() {
        return seed;
    }
}

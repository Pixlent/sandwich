package me.pixlent;

import me.pixlent.noise.InterpolatedNoiseRegistry;
import me.pixlent.noise.NoiseRegistry;
import me.pixlent.utils.SplineInterpolator;

public class ExampleTerrainBuilder extends TerrainBuilder {
    private final SplineInterpolator splineInterpolator = SplineInterpolator.builder()
            //.add(-64, 6)
            //.add(20, 1.7)
            //.add(70, 1)
            //.add(200, 0.4)
            .add(-64, 4)
            .add(0, 2)
            .add(64, 1)
            .add(96, 0.7)
            .add(140, 0.6)
            .add(170, 0.4)
            .add(210, 0.3)
            .add(256, 0)
            .build();

    ExampleTerrainBuilder(long seed) {
        super(seed);
    }

    @Override
    int getSurfaceHeight(int x, int z) {
        double continentalness = InterpolatedNoiseRegistry.CONTINENTALNESS.evaluateNoise(x, z);
        double erosion = InterpolatedNoiseRegistry.EROSION.evaluateNoise(x, z);
        double noise = (continentalness + erosion) / 2;
        //double surfaceLevel = (continentalness);

        return (int) Math.round(continentalness * 128);
    }

    @Override
    double getDensity(int x, int y, int z) {
        int surfaceHeight = getSurfaceHeight(x, z);
        int offsetY = y - surfaceHeight + 50;

        double density = NoiseRegistry.DENSITY.evaluateNoise(x*3, offsetY*5, z*3);

        //density *= (double) surfaceHeight / y;

        density *= splineInterpolator.interpolate(y);

        return density;
    }
}

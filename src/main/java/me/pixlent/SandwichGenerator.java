package me.pixlent;

import me.pixlent.cache.NoiseChunkCache;
import me.pixlent.generator.DensityFunction;
import me.pixlent.noise.FastNoiseSource;
import me.pixlent.noise.NoiseFunction;
import me.pixlent.cache.NoiseInterpolationResolution;
import me.pixlent.noise.impl.FractalNoiseFunction;
import me.pixlent.noise.modifier.AbsFunctionModifier;
import me.pixlent.noise.modifier.SplineFunctionModifier;
import me.pixlent.utils.SplineInterpolator;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

public class SandwichGenerator implements Generator {
    private final NoiseFunction densityFunction;
    private final NoiseFunction weirdnessFunction;
    private final NoiseFunction continentalnessFunction;
    private final NoiseFunction erosionFunction;
    private final SplineInterpolator heightBiasInterpolator = SplineInterpolator
            .builder()
            .add(-64, 1)
            .add(0, 0.7)
            .add(16, 0)
            .add(90, -0.3)
            .add(256, -1)
            .build();

    private SandwichGenerator(TerrainBuilder builder) {
        this.densityFunction = builder.densityFunction;

            continentalnessFunction = new FractalNoiseFunction(0, 0.0007f, FastNoiseSource.SIMPLEX, 5, 0.5f, 2.3f);
            continentalnessFunction.modifier(new SplineFunctionModifier(SplineInterpolator.builder()
                    .add(-1.0, -30)
                    .add(-0.5, -5)
                    .add(-0.3, 60)
                    .add(-0.15, 67)
                    .add(0, 73)
                    .add(0.2, 90)
                    .add(0.24, 91)
                    .add(0.3, 140)
                    .add(0.4, 142)
                    .add(0.6, 210)
                    .add(0.7, 252)
                    .add(0.8, 253)
                    .add(0.9, 256)
                    .build()));

        erosionFunction = new FractalNoiseFunction(0, 0.009f, FastNoiseSource.SIMPLEX, 5, 0.5f, 2.3f);
//        erosionFunction.modifier(new SplineFunctionModifier(SplineInterpolator.builder()
//                .add(-1.0, -1.0)
//                .add(-0.7, -0.8)
//                .add(-0.6, -0.4)
//                .add(-0.4, 0)
//                .add(-0.22, 0.2)
//                .add(-0.134, 0.554)
//                .add(0.1, 0.776)
//                .add(0.4, 0.94)
//                .add(1, 1)
//                .build()));

        weirdnessFunction = new FractalNoiseFunction(0, 0.005f, FastNoiseSource.SIMPLEX, 2, 0.2f, 0.8f);
        weirdnessFunction.modifier(new AbsFunctionModifier());
    }

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        Point min = unit.absoluteStart();
        Point size = unit.size();

        NoiseChunkCache noiseCache = new NoiseChunkCache(densityFunction, min, NoiseInterpolationResolution.Five);

//        for (int x = 0; x < size.x(); x++) {
//            for (int z = 0; z < size.z(); z++) {
//                Point bottom = min.add(x, 0, z);
//
//                float continentalness = continentalnessFunction.apply(bottom.blockX(), bottom.blockZ());
//
//                float surfaceHeight = continentalness;
//
//                unit.modifier().fill(bottom, bottom.add(1, 0, 1).withY(surfaceHeight), Block.STONE);
//                if (surfaceHeight < 64) unit.modifier().fill(bottom.withY(surfaceHeight), bottom.add(1, 0, 1).withY(63), Block.WATER);
//            }
//        }

        for (int x = 0; x < size.blockX(); x++) {
            for (int z = 0; z < size.blockZ(); z++) {
                for (int y = 0; y < size.blockY(); y++) {
                    Point world = min.add(x, y, z);

                    float surfaceHeight = continentalnessFunction.apply(world.blockX(), world.blockZ());
                    float density = noiseCache.getNoise(x, y, z);

                    density = density * 2 - 1;
                    //float weirdness = weirdnessFunction.apply(world.blockX(), world.blockZ());

                    float heightBias = world.blockY() - surfaceHeight; // Controls compression

                    density += heightBias * 3.4f;

                    //if (world.blockY() < surfaceHeight) density = 1;
                    //if (world.blockY() > surfaceHeight) density = -1;

                    Block block = Block.AIR;

                    if (world.blockY() < 64) block = Block.WATER;
                    if (density > 0) block = Block.STONE;

                    unit.modifier().setBlock(world, block);
                }
            }
        }
    }

    public static TerrainBuilder builder() {
        return new TerrainBuilder();
    }

    public static class TerrainBuilder {
        private NoiseFunction densityFunction;

        public SandwichGenerator densityFunction(NoiseFunction densityFunction) {
            this.densityFunction = densityFunction;
            return new SandwichGenerator(this);
        }
    }
}

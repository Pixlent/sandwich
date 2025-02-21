package me.pixlent.noise;

import com.github.fastnoise.FastNoise;
import com.github.fastnoise.FloatArray;
import me.pixlent.noise.applier.FunctionApplier;
import me.pixlent.noise.applier.NormalizedFunctionApplier;
import me.pixlent.noise.modifier.FunctionModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public interface FastNoiseFunction extends NoiseFunction {
    List<FunctionModifier> modifiers = new ArrayList<>();
    Map<NoiseFunction, FunctionApplier> noiseFunctions = new HashMap<>();

    FastNoise getFastNoise();
    int getSeed();
    float getFrequency();

    @Override
    default void modifier(FunctionModifier modifier) {
        modifiers.add(modifier);
    }

    @Override
    default void add(NoiseFunction noiseFunction) {
        noiseFunctions.put(noiseFunction, new NormalizedFunctionApplier());
    }

    @Override
    default void add(NoiseFunction noiseFunction, FunctionApplier applier) {
        noiseFunctions.put(noiseFunction, applier);
    }

    @Override
    default float apply(float x, float z) {
        AtomicReference<Float> value = new AtomicReference<>(getFastNoise().genSingle2D(x * getFrequency(), z * getFrequency(), getSeed()));

        modifiers.forEach(modifier -> value.set(modifier.apply(value.get())));
        noiseFunctions.forEach((addedNoiseFunction, applier) ->
                value.set(applier.apply(value.get(), addedNoiseFunction.apply(x, z))));

        return value.get();
    }

    @Override
    default float apply(float x, float y, float z) {
        AtomicReference<Float> value = new AtomicReference<>(getFastNoise().genSingle3D(x * getFrequency(), y * getFrequency(), z * getFrequency(), getSeed()));

        modifiers.forEach(modifier -> value.set(modifier.apply(value.get())));
        noiseFunctions.forEach((addedNoiseFunction, applier) ->
                value.set(applier.apply(value.get(), addedNoiseFunction.apply(x, y, z))));

        return value.get();
    }

    @Override
    default void apply(float[][] input, int xStart, int zStart, int xSize, int zSize, int xOffset, int zOffset) {
        Map<float[][], FunctionApplier> noises = new HashMap<>();

        noiseFunctions.forEach((noiseFunction, applier) -> {
            float[][] cachedNoise = new float[xSize][zSize];

            noiseFunction.apply(cachedNoise, xStart, zStart, xSize, zSize, xOffset, zOffset);

            noises.put(cachedNoise, applier);
        });

        int totalSize = xSize * zSize;

        FloatArray noiseOut = new FloatArray(totalSize);
        FloatArray xPosArray = new FloatArray(totalSize);
        FloatArray zPosArray = new FloatArray(totalSize);

        int index = 0;
        for (int x = 0; x < xSize; x++) {
            for (int z = 0; z < zSize; z++) {
                xPosArray.set(index, (xStart + (x*xOffset)) * getFrequency());
                zPosArray.set(index, (zStart + (z*zOffset)) * getFrequency());
                index++;
            }
        }

        getFastNoise().genPositionArray2D(noiseOut, xPosArray, zPosArray, 0f, 0f, getSeed());

        index = 0;
        for (int x = 0; x < xSize; x++) {
            for (int z = 0; z < zSize; z++) {
                AtomicReference<Float> noise = new AtomicReference<>(noiseOut.get(index));

                int finalX = x;
                int finalZ = z;

                modifiers.forEach(modifier ->
                        noise.set(modifier.apply(noise.get())));
                noises.forEach((cachedNoise, applier) ->
                        noise.set(applier.apply(noise.get(), cachedNoise[finalX][finalZ])));

                input[x][z] = noise.get();
                index++;
            }
        }
    }

    @Override
    default void apply(float[][][] input, int xStart, int yStart, int zStart, int xSize, int ySize, int zSize, int xOffset, int yOffset, int zOffset) {
        Map<float[][][], FunctionApplier> noises = new HashMap<>();

        noiseFunctions.forEach((noiseFunction, applier) -> {
            float[][][] cachedNoise = new float[xSize][ySize][zSize];

            noiseFunction.apply(cachedNoise, xStart, yStart, zStart, xSize, ySize, zSize, xOffset, yOffset, zOffset);

            noises.put(cachedNoise, applier);
        });

        int totalSize = xSize * ySize * zSize;

        FloatArray noiseOut = new FloatArray(totalSize);
        FloatArray xPosArray = new FloatArray(totalSize);
        FloatArray yPosArray = new FloatArray(totalSize);
        FloatArray zPosArray = new FloatArray(totalSize);

        int index = 0;
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                for (int z = 0; z < zSize; z++) {
                    xPosArray.set(index, (xStart + (x*xOffset)) * getFrequency());
                    yPosArray.set(index, (yStart + (y*yOffset)) * getFrequency());
                    zPosArray.set(index, (zStart + (z*zOffset)) * getFrequency());
                    index++;
                }
            }
        }

        getFastNoise().genPositionArray3D(noiseOut, xPosArray, yPosArray, zPosArray, 0f, 0f, 0f, getSeed());

        index = 0;
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                for (int z = 0; z < zSize; z++) {
                    AtomicReference<Float> noise = new AtomicReference<>(noiseOut.get(index));

                    int finalX = x;
                    int finalY = y;
                    int finalZ = z;

                    modifiers.forEach(modifier ->
                            noise.set(modifier.apply(noise.get())));
                    noises.forEach((cachedNoise, applier) ->
                            noise.set(applier.apply(noise.get(), cachedNoise[finalX][finalY][finalZ])));

                    input[x][y][z] = noise.get();
                    index++;
                }
            }
        }
    }
}

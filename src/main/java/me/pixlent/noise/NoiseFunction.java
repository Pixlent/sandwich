package me.pixlent.noise;

import me.pixlent.noise.applier.FunctionApplier;
import me.pixlent.noise.modifier.FunctionModifier;

public interface NoiseFunction {
    /**
     * Apply returns the noise at the x, z coordinate and applies any additional functions to the noise which have been added.
     * @param x The x-coordinate to sample from.
     * @param z The z-coordinate to sample from.
     * @return The calculated and applied noise value.
     */

    float apply(float x, float z);
    /**
     * Apply returns the noise at the x, y, z coordinate and applies any additional functions to the noise which have been added.
     * @param x The x-coordinate to sample from.
     * @param y The y-coordinate to sample from.
     * @param z The z-coordinate to sample from.
     * @return The calculated and applied noise value.
     */
    float apply(float x, float y, float z);

    /**
     * Applies all the modifiers to the noise and adds those values to the input-array
     * @param input The input array to apply the noise to.
     * @param xStart The x-start position for the noise to originate from.
     * @param zStart The z-start position for the noise to originate from.
     * @param xSize The x- "size" or "length" at which to calculate to.
     * @param zSize The z- "size" or "length" at which to calculate to.
     * @param xOffset An offset on the x-axis (mostly useful if you plan on doing interpolation).
     * @param zOffset An offset on the z-axis (mostly useful if you plan on doing interpolation).
     */
    void apply(float[][] input, int xStart, int zStart, int xSize, int zSize, int xOffset, int zOffset);

    /**
     * Applies all the modifiers to the noise and adds those values to the input-array
     * @param input The input array to apply the noise to.
     * @param xStart The x-start position for the noise to originate from.
     * @param yStart The y-start position for the noise to originate from.
     * @param zStart The z-start position for the noise to originate from.
     * @param xSize The x- "size" or "length" at which to calculate to.
     * @param ySize The y- "size" or "length" at which to calculate to.
     * @param zSize The z- "size" or "length" at which to calculate to.
     * @param xOffset An offset on the x-axis (mostly useful if you plan on doing interpolation).
     * @param yOffset An offset on the y-axis (mostly useful if you plan on doing interpolation).
     * @param zOffset An offset on the z-axis (mostly useful if you plan on doing interpolation).
     */
    void apply(float[][][] input, int xStart, int yStart, int zStart, int xSize, int ySize, int zSize, int xOffset, int yOffset, int zOffset);

    void modifier(FunctionModifier modifier);
    void add(NoiseFunction noiseFunction);
    void add(NoiseFunction noiseFunction, FunctionApplier applier);
}

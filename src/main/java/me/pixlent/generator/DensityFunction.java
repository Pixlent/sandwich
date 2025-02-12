package me.pixlent.generator;

@FunctionalInterface
public interface DensityFunction {
    float apply(float x, float y, float z);
}

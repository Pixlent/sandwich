package me.pixlent.noise.applier;

@FunctionalInterface
public interface FunctionApplier {
    float apply(float base, float add);
}

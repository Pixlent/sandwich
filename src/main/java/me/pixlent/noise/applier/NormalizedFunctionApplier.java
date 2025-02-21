package me.pixlent.noise.applier;

public class NormalizedFunctionApplier implements FunctionApplier {
    @Override
    public float apply(float base, float add) {
        return (base + add) / 2;
    }
}

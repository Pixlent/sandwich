package me.pixlent.noise.applier;

public class WeightedFunctionApplier implements FunctionApplier {
    private final float weight;

    public WeightedFunctionApplier(float weight) {
        this.weight = weight;
    }

    @Override
    public float apply(float base, float add) {
        return base + (add * weight);
    }
}

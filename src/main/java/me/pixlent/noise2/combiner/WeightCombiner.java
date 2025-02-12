package me.pixlent.noise2.combiner;

public class WeightCombiner implements LayerCombiner {
    private final float weight;

    public WeightCombiner(float weight) {
        this.weight = weight;
    }

    @Override
    public float apply(float base, float layer) {
        return base + (layer * weight);
    }
}

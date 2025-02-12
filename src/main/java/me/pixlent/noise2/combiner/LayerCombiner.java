package me.pixlent.noise2.combiner;

@FunctionalInterface
public interface LayerCombiner {
    float apply(float base, float layer);
}

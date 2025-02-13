package me.pixlent.noise2;

import me.pixlent.noise2.combiner.LayerCombiner;
import me.pixlent.noise2.combiner.WeightCombiner;
import me.pixlent.noise2.modifier.NoiseModifier;

import java.sql.Array;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class NoiseStack extends NoiseLayer {
    private final Map<NoiseLayer, LayerCombiner> layers = new LinkedHashMap<>(); // linked hashmap to preserve order
    private final List<NoiseModifier> modifiers = new CopyOnWriteArrayList<>();

    public void addLayer(NoiseLayer layer, LayerCombiner combiner) {
        layers.put(layer, combiner);
    }

    public void addLayer(NoiseLayer layer) {
        addLayer(layer, new WeightCombiner(1f));
    }

    public void addModifier(NoiseModifier modifier) {
        modifiers.add(modifier);
    }

    @Override
    public float sample(float x, float y, float z) {
        float noise = 0f;

        for (var entry : layers.entrySet()) {
            NoiseLayer layer = entry.getKey();
            LayerCombiner combiner = entry.getValue();
            noise = (combiner.apply(noise, layer.sample(x, y, z)));
        }
        for (var modifier : modifiers) {
            noise = (modifier.apply(noise));
        }

        return noise;
    }

    @Override
    public float sample(float x, float z) {
        float noise = 0f;

        for (var entry : layers.entrySet()) {
            NoiseLayer layer = entry.getKey();
            LayerCombiner combiner = entry.getValue();
            noise = (combiner.apply(noise, layer.sample(x, z)));
        }
        for (var modifier : modifiers) {
            noise = (modifier.apply(noise));
        }

        return noise;
    }
}
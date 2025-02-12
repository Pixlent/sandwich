package me.pixlent.noise2;

import me.pixlent.noise2.combiner.LayerCombiner;
import me.pixlent.noise2.combiner.WeightCombiner;
import me.pixlent.noise2.modifier.NoiseModifier;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class NoiseStack extends NoiseLayer {
    private final Map<NoiseLayer, LayerCombiner> layers = new HashMap<>();
    private final List<NoiseModifier> modifiers = new ArrayList<>();

    public void addLayer(NoiseLayer layer, LayerCombiner combiner) {
        layers.put(layer, combiner);
    }

    public void addLayer(NoiseLayer layer) {
        layers.put(layer, new WeightCombiner(1f));
    }

    public void addModifier(NoiseModifier modifier) {
        modifiers.add(modifier);
    }

    @Override
    public float sample(float x, float y, float z) {
        AtomicReference<Float> noise = new AtomicReference<>(0f);

        layers.forEach((layer, combiner) ->
                noise.set(combiner.apply(noise.get(), layer.sample(x, y, z))));
        modifiers.forEach(modifier ->
                noise.set(modifier.apply(noise.get())));

        return noise.get();
    }

    @Override
    public float sample(float x, float z) {
        AtomicReference<Float> noise = new AtomicReference<>(0f);

        layers.forEach((layer, combiner) ->
                noise.set(combiner.apply(noise.get(), layer.sample(x, z))));
        modifiers.forEach(modifier ->
                noise.set(modifier.apply(noise.get())));

        return noise.get();
    }
}
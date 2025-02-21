package me.pixlent.voxel.noise;

import com.github.fastnoise.FastNoise;
import me.pixlent.voxel.Voxel;

public interface NoiseCommon {
    FastNoise getFastNoise();
    float getFrequency();
    int getSeed();
    int getSpacing(); // Resolution for interpolation
    Voxel getMin();
}

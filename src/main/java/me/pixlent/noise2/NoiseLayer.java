package me.pixlent.noise2;

public abstract class NoiseLayer {
    abstract float sample(float x, float y, float z);
    abstract float sample(float x, float z);
}

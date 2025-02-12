package me.pixlent.generator;

public interface TerrainBuilder {
    int getSeed();
    float getDensity(int x, int y, int z);
}

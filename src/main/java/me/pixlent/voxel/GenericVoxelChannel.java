package me.pixlent.voxel;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.UnaryOperator;

public class GenericVoxelChannel<T> implements VoxelChannel<T> {
    private final T[] data;
    private final int width, height, depth;

    @SuppressWarnings("unchecked")
    public GenericVoxelChannel(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.data = (T[]) new Object[width * height * depth];
    }

    private int index(int x, int y, int z) {
        return (x * height * depth) + (y * depth) + z;
    }

    @Override
    public T get(int x, int y, int z) {
        return data[index(x, y, z)];
    }

    @Override
    public boolean has(int x, int y, int z) {
        return data[index(x, y, z)] != null;
    }

    @Override
    public void set(int x, int y, int z, T value) {
        data[index(x, y, z)] = value;
    }

    @Override
    public void fill(T value) {
        Arrays.fill(data, value);
    }

    @Override
    public void apply(UnaryOperator<T> operator) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] != null) {
                data[i] = operator.apply(data[i]);
            }
        }
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getDepth() {
        return depth;
    }
}
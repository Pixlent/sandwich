package me.pixlent.voxel;

import java.util.Arrays;
import java.util.function.UnaryOperator;

public class GenericPixelChannel<T> implements PixelChannel<T> {
    private final T[] data;
    private final int width, depth;

    @SuppressWarnings("unchecked")
    public GenericPixelChannel(int width, int depth) {
        this.width = width;
        this.depth = depth;
        this.data = (T[]) new Object[width * depth];
    }

    private int index(int x, int z) {
        return (x * depth) + z;
    }

    @Override
    public T get(int x, int z) {
        return data[index(x, z)];
    }

    @Override
    public boolean has(int x, int z) {
        return data[index(x, z)] != null;
    }

    @Override
    public void set(int x, int z, T value) {
        data[index(x, z)] = value;
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
    public int getDepth() {
        return depth;
    }
}
package me.pixlent.voxel;

import java.util.function.UnaryOperator;

public interface Channel<T> {
    void fill(T value);
    void apply(UnaryOperator<T> operator);
    int getWidth();
    int getDepth();
}

package me.pixlent.voxel;

import org.jetbrains.annotations.NotNull;

public interface PixelChannel<T> extends Channel<T> {
    T get(int x, int z);
    boolean has(int x, int z);
    void set(int x, int z, T value);

    default void setAll(PixelChannel.PixelSupplier<T> supplier) {
        for (int x = 0; x < getWidth(); x++) {
            for (int z = 0; z < getDepth(); z++) {
                set(x, z, supplier.get(x, z));
            }
        }
    }

    default void fill(int xStart, int zStart, int xSize, int zSize, T value) {
        for (int x = xStart; x < xStart + xSize; x++) {
            for (int z = zStart; z < zStart + zSize; z++) {
                set(x, z, value);
            }
        }
    }

    @FunctionalInterface
    interface PixelSupplier<T> {
        @NotNull T get(int x, int z);
    }
}

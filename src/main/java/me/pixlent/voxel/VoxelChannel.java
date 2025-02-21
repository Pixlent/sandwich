package me.pixlent.voxel;

import org.jetbrains.annotations.NotNull;

public interface VoxelChannel<T> extends Channel<T> {
    T get(int x, int y, int z);
    boolean has(int x, int y, int z);
    void set(int x, int y, int z, T value);

    int getHeight();

    default void setAll(VoxelSupplier<T> supplier) {
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                for (int z = 0; z < getDepth(); z++) {
                    set(x, y, z, supplier.get(x, y, z));
                }
            }
        }
    }

    default void fill(int xStart, int yStart, int zStart, int xSize, int ySize, int zSize, T value) {
        for (int x = xStart; x < xStart + xSize; x++) {
            for (int y = yStart; y < yStart + ySize; y++) {
                for (int z = zStart; z < zStart + zSize; z++) {
                    set(x, y, z, value);
                }
            }
        }
    }

    default void fillHeight(int minHeight, int maxHeight, T value) {
        for (int x = 0; x < getWidth(); x++) {
            for (int z = 0; z < getDepth(); z++) {
                for (int y = minHeight; y <= maxHeight; y++) {
                    set(x, y, z, value);
                }
            }
        }
    }

    @FunctionalInterface
    interface VoxelSupplier<T> {
        @NotNull T get(int x, int y, int z);
    }
}
package me.pixlent.voxel;

import net.minestom.server.coordinate.Point;

import java.util.Map;

public class VoxelContext {
    public final Voxel min;
    public final Voxel size;
    private final Map<String, Channel<?>> channels;

    VoxelContext(Voxel min, Voxel size, Map<String, Channel<?>> channels) {
        this.min = min;
        this.size = size;
        this.channels = channels;
    }

    @SuppressWarnings("unchecked")
    public <T> Channel<T> getChannel(String name, Class<T> ignoredType) {
        return (Channel<T>) channels.get(name);
    }
}
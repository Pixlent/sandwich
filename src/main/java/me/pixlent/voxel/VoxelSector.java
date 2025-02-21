package me.pixlent.voxel;

import me.pixlent.voxel.procedural.Procedural;
import net.minestom.server.coordinate.Point;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VoxelSector {
    private final VoxelContext context;
    private final Map<String, Channel<?>> channels = new ConcurrentHashMap<>();

    public VoxelSector(Voxel min, Voxel size) {
        this.context = new VoxelContext(min, size, channels);
    }

    public <T> void createPixelChannel(String name, Class<T> ignoredType) {
        channels.put(name, new GenericPixelChannel<T>(context.size.x(), context.size.z()));
    }

    public <T> void createVoxelChannel(String name, Class<T> ignoredType) {
        channels.put(name, new GenericVoxelChannel<T>(context.size.x(), context.size.y(), context.size.z()));
    }

    public <T> void createChannel(String name, Channel<T> channel) {
        channels.put(name, channel);
    }

    public void deleteChannel(String name) {
        channels.remove(name);
    }

    public void apply(Procedural procedural) {
        procedural.apply(context);
    }
}
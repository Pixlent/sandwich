package me.pixlent.voxel.procedural;

import me.pixlent.voxel.VoxelContext;
import me.pixlent.voxel.VoxelSector;

@FunctionalInterface
public interface Procedural {
    void apply(VoxelContext context);
}

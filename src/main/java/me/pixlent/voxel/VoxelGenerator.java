package me.pixlent.voxel;

import com.github.fastnoise.FastNoise;
import me.pixlent.noise.FastNoiseSource;
import me.pixlent.voxel.noise.NoisePixelChannel;
import me.pixlent.voxel.noise.NoiseVoxelChannel;
import me.pixlent.voxel.procedural.ProcBlockPlacer;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class VoxelGenerator implements Generator {
    private final FastNoise whiteNoise = new FastNoise("White");
    private final FastNoise surfaceNoise = new FastNoise("FractalFBm");
    private final FastNoise clusterNoise = new FastNoise("FractalFBm");
    private final FastNoise densityNoise = new FastNoise("FractalFBm");

    public VoxelGenerator() {
        surfaceNoise.set("Source", FastNoiseSource.SIMPLEX.getSource());
        surfaceNoise.set("Octaves", 6);
        surfaceNoise.set("Gain", 0.36f);
        surfaceNoise.set("Lacunarity", 2.4f);

        clusterNoise.set("Source", FastNoiseSource.SIMPLEX.getSource());
        clusterNoise.set("Octaves", 2);
        clusterNoise.set("Gain", 0.3f);
        clusterNoise.set("Lacunarity", 1.4f);

        densityNoise.set("Source", FastNoiseSource.SIMPLEX.getSource());
        densityNoise.set("Octaves", 6);
        densityNoise.set("Gain", 0.4f);
        densityNoise.set("Lacunarity", 2.2f);
    }

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        final int width = unit.size().blockX();
        final int depth = unit.size().blockZ();
        final int height = unit.size().blockY();

        VoxelSector voxelSector = new VoxelSector(Voxel.from(unit.absoluteStart()), Voxel.from(unit.size()));

        NoisePixelChannel whiteChannel = new NoisePixelChannel(
                Voxel.from(unit.absoluteStart()),
                width, depth,
                whiteNoise, hash("white"), 1f, 1);
        NoisePixelChannel surfaceChannel = new NoisePixelChannel(
                Voxel.from(unit.absoluteStart()),
                width, height,
                surfaceNoise, hash("surface"), 0.0025f, 4);
        NoisePixelChannel grassChannel = new NoisePixelChannel(
                Voxel.from(unit.absoluteStart()),
                width, height,
                clusterNoise, hash("grass"), 0.015f, 4);
        NoisePixelChannel flowerChannel = new NoisePixelChannel(
                Voxel.from(unit.absoluteStart()),
                width, height,
                clusterNoise, hash("flowers"), 0.04f, 4);
        NoisePixelChannel beachTransitionChannel = new NoisePixelChannel(
                Voxel.from(unit.absoluteStart()),
                width, height,
                clusterNoise, hash("beach_transition"), 0.02f, 4);
        NoisePixelChannel oceanTransitionChannel = new NoisePixelChannel(
                Voxel.from(unit.absoluteStart()),
                width, height,
                clusterNoise, hash("ocean_transition"), 0.02f, 4);
        NoiseVoxelChannel densityChannel = new NoiseVoxelChannel(
                Voxel.from(unit.absoluteStart()),
                width, height, depth,
                densityNoise, hash("density"), 0.003f, 4);

        voxelSector.createChannel("white", whiteChannel);
        voxelSector.createChannel("surface_height", surfaceChannel);
        voxelSector.createChannel("grass", grassChannel);
        voxelSector.createChannel("flowers", flowerChannel);
        voxelSector.createChannel("beach_transition", beachTransitionChannel);
        voxelSector.createChannel("ocean_transition", oceanTransitionChannel);
        voxelSector.createChannel("density", densityChannel);

        beachTransitionChannel.apply(transition -> transition * 2);
        oceanTransitionChannel.apply(transition -> transition * 2);

        voxelSector.apply(_ -> surfaceChannel.setAll((x, z) -> {
            float surfaceHeight = surfaceChannel.get(x, z);
            return (surfaceHeight * 64f) + 128f;
        }));
        voxelSector.apply(_ -> densityChannel.setAll((x, y, z) -> {
            float density = densityChannel.get(x, y, z);
            float surfaceHeight = surfaceChannel.get(x, z);
            float bias = surfaceHeight - y;

            density += bias / 128;

            return density;
        }));

        voxelSector.apply(new ProcBlockPlacer(unit));
    }

    private static int hash(Object... objects) {
        return Objects.hash(0, Objects.hash(objects));
    }
}

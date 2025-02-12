package me.pixlent;

import me.pixlent.generator.DecorationData;
import me.pixlent.generator.IndependentTerrainDecorator;
import me.pixlent.generator.TerrainBuilder;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

public class CustomGenerator implements Generator {
    private final TerrainBuilder terrainBuilder;
    private final IndependentTerrainDecorator terrainDecorator;

    public CustomGenerator(TerrainBuilder terrainBuilder, IndependentTerrainDecorator terrainDecorator) {
        this.terrainBuilder = terrainBuilder;
        this.terrainDecorator = terrainDecorator;
    }

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        Generation generation = new Generation(unit, terrainBuilder);
        Point min = unit.absoluteStart();
        Point size = unit.size();

        // Place blocks according to the density (decorate)
        // This includes proper water aggregation
        // Surface blocks
        // Ores
        for (int x = 0; x < size.blockX(); x++) {
            for (int z = 0; z < size.blockZ(); z++) {
                for (int y = size.blockY(); y > 0; y--) {
                    Point world = min.add(x, y - 1, z);
                    float density = terrainBuilder.getDensity(world.blockX(), world.blockY(), world.blockZ());

//                    if (density > 0 && terrainBuilder.getDensity(x, y + 1, z) < 0)
//                        terrainDecorator.runSurfaceCondition(new DecorationData(new Vec(x, y, z), world, generation));
                    Block block = Block.AIR;

                    if (world.blockY() < 64) block = Block.WATER;
                    if (density > 0) block = Block.STONE;

                    unit.modifier().setBlock(world, block);
                }
            }
        }

        // Add features
    }
}
package me.pixlent;

import me.pixlent.generator.TerrainBuilder;
import me.pixlent.utils.TrilinearInterpolator;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.generator.GenerationUnit;

public class Generation {
    public final GenerationUnit unit;
    public final TerrainBuilder terrainBuilder;
    private final TrilinearInterpolator trilinearInterpolator = new TrilinearInterpolator();
    private final float[][][] cache;

    public final int CHUNK_SIZE = 16;
    public final int SAMPLE_INTERVAL = 4;

    Generation(GenerationUnit unit, TerrainBuilder terrainBuilder) {
        this.unit = unit;
        this.terrainBuilder = terrainBuilder;

        final Point min = unit.absoluteStart();
        final Point max = unit.absoluteEnd();

        cache = new float[CHUNK_SIZE][max.blockY() - min.blockY() + 3][CHUNK_SIZE];

        generateCache();
    }

    /**
     * @param x The chunk-local x-coordinate (0 - 15).
     * @param y The chunk-local y-coordinate (0 - 384).
     * @param z The chunk-local z-coordinate (0 - 15).
     */
    private void setDensity(int x, int y, int z, float density) {
        cache[x][y][z] = density;
    }

    /**
     * @param pos The chunk-local position (0 - 15, 0 - 384, 0 - 15).
     * @param density The density (should range from -1 to 1).
     */
    private void setDensity(Point pos, float density) {
        cache[pos.blockX()][pos.blockY()][pos.blockZ()] = density;
    }

    /**
     * @param x The chunk-local x-coordinate (0 - 15).
     * @param y The chunk-local y-coordinate (0 - 384).
     * @param z The chunk-local z-coordinate (0 - 15).
     */
    public float getDensity(int x, int y, int z) {
        return cache[x][y][z];
    }

    /**
     * @param pos The chunk-local position (0 - 15, 0 - 384, 0 - 15).
     * @return The density (should range from -1 to 1).
     */
    public float getDensity(Point pos) {
        return cache[pos.blockX()][pos.blockY()][pos.blockZ()];
    }

    /**
     * @param local The local chunk coordinates (0 - 15, 0 - 384, 0 - 15).
     * @param world The global world coordinates.
     * @return The density at said position.
     */
    public float getSafeDensity(Point local, Point world) {
        if (local.blockX() >= 0 && local.blockX() < cache.length &&
                local.blockY() >= 0 && local.blockY() < cache[0].length &&
                local.blockZ() >= 0 && local.blockZ() < cache[0][0].length)

            return cache[local.blockX()][local.blockY()][local.blockZ()];
        else return terrainBuilder.getDensity(world.blockX(), world.blockY(), world.blockZ());
    }

    private void generateCache() {
        Point size = unit.size();
        Point min = unit.absoluteStart();

        int mx;
        int mz;
        int my;

        for (int x = 0; x < size.blockX(); x += SAMPLE_INTERVAL - 1) {
            for (int z = 0; z < size.blockZ(); z += SAMPLE_INTERVAL - 1) {
                for (int y = 0; y < size.blockY(); y += SAMPLE_INTERVAL - 1) {
                    float density = terrainBuilder.getDensity(min.blockX() + x, min.blockY() + y, min.blockZ() + z);
                    setDensity(x, y, z, density);
                    //if (density > 0) unit.modifier().setBlock(min.blockX() + x, min.blockY() + y, min.blockZ() + z, Block.COBBLESTONE);
                }
            }
        }

//        for (int x = 0; x < size.blockX(); x++) {
//            for (int z = 0; z < size.blockZ(); z++) {
//                for (int y = 0; y < size.blockY(); y++) {
//                    float density = terrainBuilder.getDensity(min.blockX() + x, min.blockY() + y, min.blockZ() + z);
//                    //setDensity(x, y, z, density);
//                    if (density > 0) unit.modifier().setBlock(min.blockX() + x, min.blockY() + y, min.blockZ() + z, Block.STONE);
//                }
//            }
//        }

        for (int x = 0; x < size.blockX() - 1; x += SAMPLE_INTERVAL - 1) {
            for (int z = 0; z < size.blockZ() - 1; z += SAMPLE_INTERVAL - 1) {
                for (int y = 0; y < size.blockY() - 1; y += SAMPLE_INTERVAL - 1) {
                    float c000 = getDensity(x, y, z);
                    float c001 = getDensity(x, y, z + SAMPLE_INTERVAL - 1);
                    float c010 = getDensity(x, y + SAMPLE_INTERVAL - 1, z);
                    float c011 = getDensity(x, y + SAMPLE_INTERVAL - 1, z + SAMPLE_INTERVAL - 1);
                    float c100 = getDensity(x + SAMPLE_INTERVAL - 1, y, z);
                    float c101 = getDensity(x + SAMPLE_INTERVAL - 1, y, z + SAMPLE_INTERVAL - 1);
                    float c110 = getDensity(x + SAMPLE_INTERVAL - 1, y + SAMPLE_INTERVAL - 1, z);
                    float c111 = getDensity(x + SAMPLE_INTERVAL - 1, y + SAMPLE_INTERVAL - 1, z + SAMPLE_INTERVAL - 1);

                    if (x == 12) mx = 0; else mx = 1;
                    if (z == 12) mz = 0; else mz = 1;
                    if (y == 382) my = 0; else my = 1;

                    // Interpolate between sampled points
                    for (int dx = 0; dx < SAMPLE_INTERVAL - mx; dx++) {
                        for (int dz = 0; dz < SAMPLE_INTERVAL - mz; dz++) {
                            for (int dy = 0; dy < SAMPLE_INTERVAL - my; dy++) {
                                float density = trilinearInterpolator.interpolate(
                                        c000, c001, c010, c011, c100, c101, c110, c111,
                                        (float) dx / SAMPLE_INTERVAL,
                                        (float) dy / SAMPLE_INTERVAL,
                                        (float) dz / SAMPLE_INTERVAL
                                );

                                setDensity(x + dx, y + dy, z + dz, density);
                                //if (density > 0) unit.modifier().setBlock(min.blockX() + x + dx, min.blockY() + y + dy, min.blockZ() + z + dz, Block.STONE);
                            }
                        }
                    }
                }
            }
        }
    }
}
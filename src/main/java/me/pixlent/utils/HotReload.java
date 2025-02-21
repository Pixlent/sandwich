package me.pixlent.utils;

import me.pixlent.generator.ExampleTerrainBuilder;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class HotReload {

    /**
     * Sets up hot reloading.
     */
    public static AtomicReference<InstanceContainer> hotReload(Supplier<InstanceContainer> createInstanceContainer) {

        AtomicReference<InstanceContainer> instanceContainerRef = new AtomicReference<>(createInstanceContainer.get());

        AtomicLong lastHash = new AtomicLong(-1);

        // countdown before reloading
        AtomicLong reloadTime = new AtomicLong(-1);

        MinecraftServer.getSchedulerManager().submitTask(() -> {

            if (reloadTime.get() != -1 && System.currentTimeMillis() > reloadTime.get()) {
                System.out.println("Reloading instance...");
                InstanceContainer oldInstance = instanceContainerRef.get();

                for (@NotNull Chunk chunk : oldInstance.getChunks()) {
                    chunk.reset();
                }

                InstanceContainer newInstance = createInstanceContainer.get();

                MinecraftServer.getSchedulerManager().submitTask(() -> {
                    for (@NotNull Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                        player.setInstance(newInstance);
                    }
                    return TaskSchedule.stop();
                });

                instanceContainerRef.set(newInstance);
                reloadTime.set(-1);

                System.out.println("Instance reloaded.");
            }

            // Define the path to the source file of the ExampleTerrainBuilder class
            String classFilePath = ExampleTerrainBuilder.class.getProtectionDomain().getCodeSource().getLocation().getPath();

            // Convert the file path string into a Path object
            Path path = Paths.get(classFilePath.substring(1));

            long hash = Hash.hashPath(path);

            if (lastHash.get() == -1) {
                // First run
                lastHash.set(hash);
                return TaskSchedule.seconds(1);
            }

            if (lastHash.get() != hash) {
                // The file has changed
                lastHash.set(hash);
                reloadTime.set(System.currentTimeMillis() + 3000);
                System.out.println("ExampleTerrainBuilder class has changed, reloading in 3 seconds...");
            }

            return TaskSchedule.millis(500);
        });

        return instanceContainerRef;
    }
}

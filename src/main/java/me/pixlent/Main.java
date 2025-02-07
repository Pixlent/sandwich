package me.pixlent;

import me.pixlent.commands.GamemodeCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntitySpawnEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.timer.TaskSchedule;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Main {
    private static int count = 0;
    private static long value = 0;

    public static void main(String[] args) {
        // Initialization
        System.setProperty("minestom.chunk-view-distance", "32");

        MinecraftServer minecraftServer = MinecraftServer.init();
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
        InterpolatedGenerator generator = new InterpolatedGenerator(new ExampleTerrainBuilder(0), new ExampleTerrainDecorator());

        instanceContainer.setChunkSupplier(LightingChunk::new);
        instanceContainer.setGenerator(generator);
        instanceContainer.setTimeRate(0);

        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(-13, 93, 48));
        });

        globalEventHandler.addListener(EntitySpawnEvent.class, event -> {
            if (!(event.getEntity() instanceof Player player)) {
                return;
            }

            player.setGameMode(GameMode.SPECTATOR);
            player.setPermissionLevel(4);
        });

        globalEventHandler.addListener(PlayerMoveEvent.class, event -> {
            Player player = event.getPlayer();
            int x = player.getPosition().blockX();
            int y = player.getPosition().blockY();
            int z = player.getPosition().blockZ();

            double density = generator.terrainBuilder.getDensity(x, y, z);

            Component message = Component.text("Density: ")
                    .color(TextColor.fromHexString("#e2ed4a"))
                    .append(Component
                            .text(density)
                            .color(TextColor.fromHexString("#f53333")));
            //player.sendActionBar(message);
        });

        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new GamemodeCommand());

        MojangAuth.init();

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", 25565);

        MinecraftServer.getSchedulerManager().scheduleTask(()
                -> {
                    if (count == 0) return;
                    instanceContainer.getPlayers().forEach(player -> {

                        Component message = Component.text("Avg chunk gen speed: ")
                                .color(TextColor.fromHexString("#e2ed4a"))
                                .append(Component
                                        .text(value / count + "ms")
                                        .color(TextColor.fromHexString("#f53333")));
                        player.sendActionBar(message);
                    });
                    count = 0;
                    value = 0;
                },
                TaskSchedule.nextTick(), TaskSchedule.seconds(1));
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_EVEN);
        return bd.doubleValue();
    }

    public static void addTime(long time) {
        count++;
        value += time;
    }
}

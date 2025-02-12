package me.pixlent;

import me.pixlent.commands.GamemodeCommand;
import me.pixlent.generator.ExampleIndependentTerrainDecorator;
import me.pixlent.generator.ExampleTerrainDecorator;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntitySpawnEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.time.TimeUnit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    private static int count = 0;
    private static long value = 0;

    private static final AtomicReference<TickMonitor> LAST_TICK = new AtomicReference<>();

    public static void main(String[] args) {
        // Initialization
        System.setProperty("minestom.chunk-view-distance", "32");

        MinecraftServer minecraftServer = MinecraftServer.init();
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
        //InterpolatedGenerator generator = new InterpolatedGenerator(new ExampleTerrainBuilder(0), new ExampleTerrainDecorator());
        CustomGenerator generator = new CustomGenerator(
                new me.pixlent.generator.ExampleTerrainBuilder(0),
                new ExampleIndependentTerrainDecorator());

        instanceContainer.setChunkSupplier(LightingChunk::new);
        instanceContainer.setGenerator(generator);
        instanceContainer.setTimeRate(0);

        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(-13, 93, 48));
        });

        eventHandler.addListener(EntitySpawnEvent.class, event -> {
            if (!(event.getEntity() instanceof Player player)) {
                return;
            }

            player.addEffect(new Potion(PotionEffect.NIGHT_VISION, 0, -1));
            player.setGameMode(GameMode.SPECTATOR);
            player.setPermissionLevel(4);
        });

        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new GamemodeCommand());

        //MojangAuth.init();

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", 25565);

//        MinecraftServer.getSchedulerManager().scheduleTask(()
//                -> {
//                    if (count == 0) return;
//                    instanceContainer.getPlayers().forEach(player -> {
//
//                        Component message = Component.text("Avg chunk gen speed: ")
//                                .color(TextColor.fromHexString("#e2ed4a"))
//                                .append(Component
//                                        .text(value / count + "ms")
//                                        .color(TextColor.fromHexString("#f53333")));
//                        player.sendActionBar(message);
//                    });
//                    count = 0;
//                    value = 0;
//                },
//                TaskSchedule.nextTick(), TaskSchedule.seconds(1));

        eventHandler.addListener(ServerTickMonitorEvent.class, event -> LAST_TICK.set(event.getTickMonitor()));

        BenchmarkManager benchmarkManager = MinecraftServer.getBenchmarkManager();
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (LAST_TICK.get() == null || MinecraftServer.getConnectionManager().getOnlinePlayerCount() == 0)
                return;

            long ramUsage = benchmarkManager.getUsedMemory();
            ramUsage /= 1e6; // bytes to MB

            TickMonitor tickMonitor = LAST_TICK.get();
            final Component header = Component.text("RAM USAGE: " + ramUsage + " MB")
                    .append(Component.newline())
                    .append(Component.text("TICK TIME: " + MathUtils.round(tickMonitor.getTickTime(), 2) + "ms"))
                    .append(Component.newline())
                    .append(Component.text("ACQ TIME: " + MathUtils.round(tickMonitor.getAcquisitionTime(), 2) + "ms"));
            final Component footer = benchmarkManager.getCpuMonitoringMessage();
            Audiences.players().sendPlayerListHeaderAndFooter(header, footer);
        }).repeat(10, TimeUnit.SERVER_TICK).schedule();
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

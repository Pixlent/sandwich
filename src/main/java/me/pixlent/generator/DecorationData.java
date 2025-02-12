package me.pixlent.generator;

import me.pixlent.Generation;
import net.minestom.server.coordinate.Point;

public record DecorationData(
        Point local,
        Point world,
        Generation generation
) {

}

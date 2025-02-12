package me.pixlent.generator;

import me.pixlent.generator.decoration.DecorationOperation;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class IndependentTerrainDecorator {
    private final List<DecorationOperation> decorationOperations = new ArrayList<>();

    void addDecorationOperation(DecorationOperation operation) {
        decorationOperations.add(operation);
    }

    public void runSurfaceCondition(DecorationData data) {
        // execute
    }
}

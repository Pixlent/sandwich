package me.pixlent.generator.decoration;

import me.pixlent.generator.DecorationData;

import java.util.function.Consumer;

public interface DecorationOperation {
    void apply(Consumer<DecorationData> data);
}

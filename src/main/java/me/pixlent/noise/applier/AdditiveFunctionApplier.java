package me.pixlent.noise.applier;

public class AdditiveFunctionApplier implements FunctionApplier{
    @Override
    public float apply(float base, float add) {
        return base + add;
    }
}

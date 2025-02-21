package me.pixlent.noise.modifier;

public class AbsFunctionModifier implements FunctionModifier{
    @Override
    public float apply(float input) {
        return (input + 1f) * 0.5f;
    }
}

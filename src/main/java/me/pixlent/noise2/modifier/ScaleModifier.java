package me.pixlent.noise2.modifier;

public class ScaleModifier implements NoiseModifier{
    private final float scale;

    public ScaleModifier(float scale) {
        this.scale = scale;
    }

    @Override
    public float apply(float base) {
        return base * scale;
    }
}

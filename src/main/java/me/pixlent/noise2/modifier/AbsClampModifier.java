package me.pixlent.noise2.modifier;

public class AbsClampModifier implements NoiseModifier {
    @Override
    public float apply(float base) {
        return (base + 1f) * 0.5f;
    }
}

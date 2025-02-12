package me.pixlent.noise2.modifier;

import me.pixlent.utils.SplineInterpolator;

public class SplineModifier implements NoiseModifier {
    private final SplineInterpolator interpolator;

    public SplineModifier(SplineInterpolator interpolator) {
        this.interpolator = interpolator;
    }

    @Override
    public float apply(float base) {
        return (float) interpolator.interpolate(base);
    }
}

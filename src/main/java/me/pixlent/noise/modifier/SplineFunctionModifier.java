package me.pixlent.noise.modifier;

import me.pixlent.utils.SplineInterpolator;

public class SplineFunctionModifier implements FunctionModifier{
    private final SplineInterpolator spline;

    public SplineFunctionModifier(SplineInterpolator spline) {
        this.spline = spline;
    }

    @Override
    public float apply(float input) {
        return (float) spline.interpolate(input);
    }
}

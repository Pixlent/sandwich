package me.pixlent.cache;

import lombok.Getter;

@Getter
public enum NoiseInterpolationResolution {
    Three(3, 6, 128),
    Five(5, 4, 77);

    private final int upscaleFactor;
    private final int width;
    private final int height;

    NoiseInterpolationResolution(int upscaleFactor, int width, int height) {
        this.upscaleFactor = upscaleFactor;
        this.width = width;
        this.height = height;
    }
}
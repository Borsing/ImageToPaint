package dev.borsing.imagetopaint.domain;

public record RGB(int red, int green, int blue) {

    public RGB {
        if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255) {
            throw new IllegalArgumentException("RGB components must be between 0 and 255");
        }
    }
}
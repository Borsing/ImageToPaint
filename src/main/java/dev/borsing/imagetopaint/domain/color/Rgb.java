package dev.borsing.imagetopaint.domain.color;

public record Rgb(int red, int green, int blue) {

    public Rgb {
        if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255) {
            throw new IllegalArgumentException("RGB components must be between 0 and 255");
        }
    }
}

package dev.borsing.imagetopaint.domain;

/**
 * A [height][width] matrix of {@link RGB} pixels.
 */
public record Image(RGB[][] pixels) {

    public int height() {
        return pixels.length;
    }

    public int width() {
        return pixels.length == 0 ? 0 : pixels[0].length;
    }
}
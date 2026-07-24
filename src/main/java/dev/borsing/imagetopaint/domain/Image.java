package dev.borsing.imagetopaint.domain;

import dev.borsing.imagetopaint.domain.color.Rgb;

/**
 * A [height][width] matrix of {@link Rgb} pixels.
 */
public record Image(Rgb[][] pixels) {

    public int height() {
        return pixels.length;
    }

    public int width() {
        return pixels.length == 0 ? 0 : pixels[0].length;
    }
}
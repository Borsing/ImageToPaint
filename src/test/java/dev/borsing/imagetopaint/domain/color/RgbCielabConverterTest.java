package dev.borsing.imagetopaint.domain.color;

import dev.borsing.imagetopaint.domain.Image;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The RGB <-> CIELAB round trip goes through floating-point gamma companding and a 3x3 matrix
 * inversion, so exact equality isn't realistic - this asserts each channel stays within a small
 * rounding tolerance instead of being lossless like {@link
 * dev.borsing.imagetopaint.adapter.BufferedImageConverterTest}'s RGB <-> BufferedImage round trip.
 */
class RgbCielabConverterTest {

    private static final int TOLERANCE = 2;

    private final RgbCielabConverter converter = new RgbCielabConverter();

    @Test
    void roundTripPreservesColorsWithinRoundingTolerance() {
        Rgb[] colors = {
                new Rgb(0, 0, 0),
                new Rgb(255, 255, 255),
                new Rgb(128, 128, 128),
                new Rgb(255, 0, 0),
                new Rgb(0, 255, 0),
                new Rgb(0, 0, 255),
                new Rgb(18, 52, 86),
        };
        Image original = new Image(new Rgb[][] {colors});

        Cielab[][] lab = converter.toCielabMatrix(original);
        Image roundTripped = converter.fromCielabMatrix(lab);

        for (int i = 0; i < colors.length; i++) {
            Rgb expected = colors[i];
            Rgb actual = roundTripped.pixels()[0][i];

            assertTrue(Math.abs(expected.red() - actual.red()) <= TOLERANCE,
                    () -> "red drifted too much: expected " + expected + " but got " + actual);
            assertTrue(Math.abs(expected.green() - actual.green()) <= TOLERANCE,
                    () -> "green drifted too much: expected " + expected + " but got " + actual);
            assertTrue(Math.abs(expected.blue() - actual.blue()) <= TOLERANCE,
                    () -> "blue drifted too much: expected " + expected + " but got " + actual);
        }
    }
}
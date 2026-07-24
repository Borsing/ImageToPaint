package dev.borsing.imagetopaint.adapter;

import dev.borsing.imagetopaint.domain.Image;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Plain unit test - no Quarkus/CDI bootstrap needed, {@link BufferedImageConverter} has no dependencies.
 */
class BufferedImageConverterTest {

    private final BufferedImageConverter converter = new BufferedImageConverter();

    @Test
    void roundTripPreservesEveryPixelExactly() {
        int[][] colors = {
                {0x000000, 0xFFFFFF, 0x123456},
                {0xABCDEF, 0x7F7F7F, 0x00FF00}
        };
        BufferedImage original = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < colors.length; y++) {
            for (int x = 0; x < colors[y].length; x++) {
                original.setRGB(x, y, colors[y][x]);
            }
        }

        Image domainImage = converter.toDomainImage(original);
        BufferedImage roundTripped = converter.fromDomainImage(domainImage);

        for (int y = 0; y < colors.length; y++) {
            for (int x = 0; x < colors[y].length; x++) {
                assertEquals(original.getRGB(x, y), roundTripped.getRGB(x, y),
                        "pixel (" + x + "," + y + ") should be unchanged by the round trip");
            }
        }
    }
}
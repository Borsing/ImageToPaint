package dev.borsing.imagetopaint.domain.filter;

import dev.borsing.imagetopaint.domain.Image;
import dev.borsing.imagetopaint.domain.color.Rgb;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GrayScaleFilterTest {

    private final GrayScaleFilter filter = new GrayScaleFilter();

    @Test
    void blackStaysBlack() {
        assertLuma(new Rgb(0, 0, 0), 0);
    }

    @Test
    void alreadyGrayPixelIsUnchanged() {
        assertLuma(new Rgb(128, 128, 128), 128);
    }

    @Test
    void weighsGreenMostAndBlueLeast() {
        // Rec. 709 luma (0.2126R + 0.7152G + 0.0722B): a saturated green reads far brighter than a
        // saturated blue of the same intensity, matching human perception.
        assertLuma(new Rgb(255, 0, 0), 54);
        assertLuma(new Rgb(0, 255, 0), 182);
        assertLuma(new Rgb(0, 0, 255), 18);
    }

    @Test
    void everyResultPixelHasEqualChannels() {
        Image source = new Image(new Rgb[][] {{new Rgb(10, 200, 40), new Rgb(90, 90, 200)}});

        Image result = filter.filter(source);

        for (Rgb[] row : result.pixels()) {
            for (Rgb pixel : row) {
                assertEquals(pixel.red(), pixel.green());
                assertEquals(pixel.green(), pixel.blue());
            }
        }
    }

    @Test
    void preservesImageDimensions() {
        Image source = new Image(new Rgb[][] {
                {new Rgb(1, 2, 3), new Rgb(4, 5, 6), new Rgb(7, 8, 9)},
                {new Rgb(9, 8, 7), new Rgb(6, 5, 4), new Rgb(3, 2, 1)}
        });

        Image result = filter.filter(source);

        assertEquals(source.width(), result.width());
        assertEquals(source.height(), result.height());
    }

    private void assertLuma(Rgb color, int expectedLuma) {
        Image source = new Image(new Rgb[][] {{color}});

        Image result = filter.filter(source);

        Rgb pixel = result.pixels()[0][0];
        assertEquals(expectedLuma, pixel.red());
        assertEquals(expectedLuma, pixel.green());
        assertEquals(expectedLuma, pixel.blue());
    }
}

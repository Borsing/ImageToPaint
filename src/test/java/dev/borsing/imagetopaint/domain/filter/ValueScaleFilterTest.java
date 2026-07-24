package dev.borsing.imagetopaint.domain.filter;

import dev.borsing.imagetopaint.domain.Image;
import dev.borsing.imagetopaint.domain.color.Rgb;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValueScaleFilterTest {

    /** RGB<->CIELAB round trips through floating-point gamma companding, so allow a small drift. */
    private static final int GRAY_TOLERANCE = 2;

    @Test
    void everyResultPixelIsEffectivelyGray() {
        Image source = new Image(new Rgb[][] {
                {new Rgb(255, 0, 0), new Rgb(0, 255, 0)},
                {new Rgb(0, 0, 255), new Rgb(120, 200, 40)}
        });
        ValueScaleFilter filter = new ValueScaleFilter(new ValueScaleFilterParams(2));

        Image result = filter.filter(source);

        for (Rgb[] row : result.pixels()) {
            for (Rgb pixel : row) {
                assertTrue(Math.abs(pixel.red() - pixel.green()) <= GRAY_TOLERANCE
                                && Math.abs(pixel.green() - pixel.blue()) <= GRAY_TOLERANCE,
                        () -> "expected an effectively gray pixel but got " + pixel);
            }
        }
    }

    @Test
    void neverProducesMoreThanTheRequestedNumberOfValues() {
        Image source = new Image(new Rgb[][] {
                {new Rgb(10, 10, 10), new Rgb(60, 60, 60), new Rgb(120, 120, 120)},
                {new Rgb(180, 180, 180), new Rgb(220, 220, 220), new Rgb(255, 255, 255)}
        });
        ValueScaleFilter filter = new ValueScaleFilter(new ValueScaleFilterParams(2));

        Image result = filter.filter(source);

        Set<Rgb> distinctColors = new LinkedHashSet<>();
        for (Rgb[] row : result.pixels()) {
            distinctColors.addAll(Arrays.asList(row));
        }
        assertTrue(distinctColors.size() <= 2, "expected at most 2 shades but got " + distinctColors);
    }

    @Test
    void sameParamsProduceTheSameResult() {
        Image source = new Image(new Rgb[][] {
                {new Rgb(12, 34, 56), new Rgb(78, 90, 123)},
                {new Rgb(200, 10, 5), new Rgb(6, 200, 10)}
        });
        ValueScaleFilter filter = new ValueScaleFilter(new ValueScaleFilterParams(2));

        Image firstRun = filter.filter(source);
        Image secondRun = filter.filter(source);

        assertTrue(Arrays.deepEquals(firstRun.pixels(), secondRun.pixels()));
    }

    @Test
    void forwardsSeedAndMaxIterationsToThePaintingStep() {
        Image source = new Image(new Rgb[][] {
                {new Rgb(10, 10, 10), new Rgb(60, 60, 60), new Rgb(120, 120, 120)},
                {new Rgb(180, 180, 180), new Rgb(220, 220, 220), new Rgb(255, 255, 255)}
        });
        ValueScaleFilterParams params = new ValueScaleFilterParams(2, 123, 3);

        Image result = new ValueScaleFilter(params).filter(source);

        Image expected = new PaintingFilter(new PaintingFilterParams(2, 123, 3))
                .filter(new GrayScaleFilter().filter(source));
        assertTrue(Arrays.deepEquals(result.pixels(), expected.pixels()),
                "ValueScaleFilter must use its own seed/maxIterations, not PaintingFilterParams's defaults");
    }

    @Test
    void preservesImageDimensions() {
        Image source = new Image(new Rgb[][] {
                {new Rgb(1, 2, 3), new Rgb(4, 5, 6), new Rgb(7, 8, 9)},
                {new Rgb(9, 8, 7), new Rgb(6, 5, 4), new Rgb(3, 2, 1)}
        });
        ValueScaleFilter filter = new ValueScaleFilter(new ValueScaleFilterParams(2));

        Image result = filter.filter(source);

        assertEquals(source.width(), result.width());
        assertEquals(source.height(), result.height());
    }
}

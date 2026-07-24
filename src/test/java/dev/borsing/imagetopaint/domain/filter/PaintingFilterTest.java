package dev.borsing.imagetopaint.domain.filter;

import dev.borsing.imagetopaint.domain.Image;
import dev.borsing.imagetopaint.domain.color.Rgb;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaintingFilterTest {

    @Test
    void reducingToOneColorProducesAUniformImage() {
        Image source = new Image(new Rgb[][] {
                {new Rgb(10, 20, 30), new Rgb(200, 100, 50)},
                {new Rgb(0, 0, 0), new Rgb(255, 255, 255)}
        });
        PaintingFilter filter = new PaintingFilter(new PaintingFilterParams(1, 7, 10));

        Image result = filter.filter(source);

        assertEquals(1, distinctColors(result).size(), "k=1 should collapse every pixel to the same color");
    }

    @Test
    void neverProducesMoreThanTheRequestedNumberOfColors() {
        Image source = new Image(new Rgb[][] {
                {new Rgb(255, 0, 0), new Rgb(0, 255, 0), new Rgb(0, 0, 255)},
                {new Rgb(255, 255, 0), new Rgb(0, 255, 255), new Rgb(255, 0, 255)}
        });
        int sourceColorCount = distinctColors(source).size();
        PaintingFilter filter = new PaintingFilter(new PaintingFilterParams(3, 7, 20));

        Image result = filter.filter(source);

        Set<Rgb> resultColors = distinctColors(result);
        assertTrue(resultColors.size() <= 3, "expected at most 3 colors but got " + resultColors);
        assertTrue(resultColors.size() < sourceColorCount, "expected real quantization to happen");
    }

    @Test
    void sameSeedProducesTheSameResult() {
        Image source = new Image(new Rgb[][] {
                {new Rgb(12, 34, 56), new Rgb(78, 90, 123)},
                {new Rgb(200, 10, 5), new Rgb(6, 200, 10)}
        });
        PaintingFilter filter = new PaintingFilter(new PaintingFilterParams(2, 99, 15));

        Image firstRun = filter.filter(source);
        Image secondRun = filter.filter(source);

        assertTrue(Arrays.deepEquals(firstRun.pixels(), secondRun.pixels()));
    }

    @Test
    void preservesImageDimensions() {
        Image source = new Image(new Rgb[][] {
                {new Rgb(1, 2, 3), new Rgb(4, 5, 6), new Rgb(7, 8, 9)},
                {new Rgb(9, 8, 7), new Rgb(6, 5, 4), new Rgb(3, 2, 1)}
        });
        PaintingFilter filter = new PaintingFilter(new PaintingFilterParams(2, 1, 10));

        Image result = filter.filter(source);

        assertEquals(source.width(), result.width());
        assertEquals(source.height(), result.height());
    }

    private Set<Rgb> distinctColors(Image image) {
        Set<Rgb> colors = new LinkedHashSet<>();
        for (Rgb[] row : image.pixels()) {
            colors.addAll(Arrays.asList(row));
        }
        return colors;
    }
}

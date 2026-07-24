package dev.borsing.imagetopaint.domain.filter;

import dev.borsing.imagetopaint.domain.Image;
import dev.borsing.imagetopaint.domain.RGB;

/**
 * Converts to grayscale using Rec. 709 relative luminance ({@code 0.2126R + 0.7152G + 0.0722B}), the
 * weighting used for HD video/sRGB. It weighs green highest and blue lowest because the human eye is
 * far more sensitive to green light than to blue — a flat average of the three channels would look
 * noticeably off (too dark where the image is green, too light where it's blue).
 */
public record GrayScaleFilter() implements ImageFilter {

    @Override
    public Image filter(Image imageSource) {
        RGB[][] source = imageSource.pixels();
        RGB[][] result = new RGB[source.length][];

        for (int y = 0; y < source.length; y++) {
            result[y] = new RGB[source[y].length];
            for (int x = 0; x < source[y].length; x++) {
                RGB pixel = source[y][x];
                int luma = (int) (pixel.red() * 0.2126 + pixel.green() * 0.7152 + pixel.blue() * 0.0722);
                result[y][x] = new RGB(luma, luma, luma);
            }
        }

        return new Image(result);
    }
}
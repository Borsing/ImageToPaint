package dev.borsing.imagetopaint.domain.filter;

import dev.borsing.imagetopaint.domain.Image;
import dev.borsing.imagetopaint.domain.color.Cielab;
import dev.borsing.imagetopaint.domain.color.RgbCielabConverter;

public record PaintingFilter(PaintingFilterParams params) implements ImageFilter {

    @Override
    public Image filter(Image imageSource) {
        RgbCielabConverter rgbCielabConverter = new RgbCielabConverter();
        Cielab[][] cielabSource = rgbCielabConverter.toCielabMatrix(imageSource);

        return rgbCielabConverter.fromCielabMatrix(cielabSource);
    }
}
package dev.borsing.imagetopaint.domain.filter;

import dev.borsing.imagetopaint.domain.Image;
import dev.borsing.imagetopaint.domain.color.Cielab;
import dev.borsing.imagetopaint.domain.color.RgbCielabConverter;

import java.util.*;

public record ValueScaleFilter(ValueScaleFilterParams params) implements ImageFilter {

    @Override
    public Image filter(Image imageSource) {
        Image grayScaleImage = new GrayScaleFilter().filter(imageSource);
        return new PaintingFilter(new PaintingFilterParams(params.numberOfValues())).filter(grayScaleImage);
    }
}
package dev.borsing.imagetopaint.domain.filter;

import dev.borsing.imagetopaint.domain.Image;

public record ValueScaleFilter(ValueScaleFilterParams params) implements ImageFilter {

    @Override
    public Image filter(Image imageSource) {
        Image grayScaleImage = new GrayScaleFilter().filter(imageSource);
        PaintingFilterParams paintingParams =
                new PaintingFilterParams(params.numberOfValues(), params.seed(), params.maxIterations());
        return new PaintingFilter(paintingParams).filter(grayScaleImage);
    }
}
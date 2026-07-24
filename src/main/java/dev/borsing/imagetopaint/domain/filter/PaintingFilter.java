package dev.borsing.imagetopaint.domain.filter;

import dev.borsing.imagetopaint.domain.Image;

public record PaintingFilter() implements ImageFilter {


    @Override
    public Image filter(Image imageSource) {
        return imageSource;
    }
}

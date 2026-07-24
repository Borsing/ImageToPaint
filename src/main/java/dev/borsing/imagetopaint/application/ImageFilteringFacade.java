package dev.borsing.imagetopaint.application;

import dev.borsing.imagetopaint.domain.filter.PaintingFilter;
import dev.borsing.imagetopaint.domain.filter.PaintingFilterParams;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.borsing.imagetopaint.adapter.BufferedImageConverter;
import dev.borsing.imagetopaint.domain.filter.GrayScaleFilter;
import dev.borsing.imagetopaint.domain.Image;

import java.awt.image.BufferedImage;

@ApplicationScoped
public class ImageFilteringFacade {

    @Inject
    BufferedImageConverter bufferedImageConverter;

    public BufferedImage filterToGrayScale(BufferedImage image) {
        Image source = bufferedImageConverter.toDomainImage(image);
        Image result = new GrayScaleFilter().filter(source);
        return bufferedImageConverter.fromDomainImage(result);
    }

    public BufferedImage filterToPaint(BufferedImage image, PaintingFilterParams params) {
        Image source = bufferedImageConverter.toDomainImage(image);
        Image result = new PaintingFilter(params).filter(source);
        return bufferedImageConverter.fromDomainImage(result);
    }
}

package dev.borsing.imagetopaint.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.borsing.imagetopaint.domain.filter.BlackAndWhiteFilter;
import dev.borsing.imagetopaint.domain.Image;

import java.awt.image.BufferedImage;

@ApplicationScoped
public class ImageFilteringService {

    @Inject
    ImageMapperService imageMapperService;

    public BufferedImage filterToBlackAndWhite(BufferedImage image) {
        Image source = imageMapperService.toDomainImage(image);
        Image result = new BlackAndWhiteFilter().filter(source);
        return imageMapperService.fromDomainImage(result);
    }
}

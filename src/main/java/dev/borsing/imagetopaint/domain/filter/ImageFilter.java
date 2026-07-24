package dev.borsing.imagetopaint.domain.filter;

import dev.borsing.imagetopaint.domain.Image;

/**
 * A pure filter from one {@link Image} to another. Implementations must not mutate the input
 * image's pixel matrix.
 */
public interface ImageFilter {

    Image filter(Image imageSource);
}
package dev.borsing.imagetopaint.domain.filter;


public record PaintingFilterParams(int numberOfColors,
                                   long seed,
                                   int maxIterations) {
}
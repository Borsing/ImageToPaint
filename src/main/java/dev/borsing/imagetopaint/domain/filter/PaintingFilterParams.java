package dev.borsing.imagetopaint.domain.filter;


public record PaintingFilterParams(int numberOfColors,
                                   long seed,
                                   int maxIterations) {

    public PaintingFilterParams(int numberOfColors) {
        this(numberOfColors, 42, 10);
    }
}
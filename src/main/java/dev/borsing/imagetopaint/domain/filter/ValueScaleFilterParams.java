package dev.borsing.imagetopaint.domain.filter;


public record ValueScaleFilterParams(int numberOfValues,
                                     long seed,
                                     int maxIterations) {

    public ValueScaleFilterParams(int numberOfValues) {
        this(numberOfValues, 42, 10);
    }
}
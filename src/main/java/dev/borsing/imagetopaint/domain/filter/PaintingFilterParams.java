package dev.borsing.imagetopaint.domain.filter;

public record PaintingFilterParams(int numberOfColors,
                                    long seed,
                                    int maxIterations) {

    private static final int MAX_NUMBER_OF_COLORS = 1000;

    /** Generous upper bound on k-means iterations - well past where it would have converged. */
    private static final int MAX_ITERATIONS_LIMIT = 100;

    public PaintingFilterParams {
        if (numberOfColors < 1 || numberOfColors > MAX_NUMBER_OF_COLORS) {
            throw new IllegalArgumentException(
                    "numberOfColors must be between 1 and " + MAX_NUMBER_OF_COLORS);
        }
        if (maxIterations < 1 || maxIterations > MAX_ITERATIONS_LIMIT) {
            throw new IllegalArgumentException(
                    "maxIterations must be between 1 and " + MAX_ITERATIONS_LIMIT);
        }
    }

    public PaintingFilterParams(int numberOfColors) {
        this(numberOfColors, 42, 10);
    }
}
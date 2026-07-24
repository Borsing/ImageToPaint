package dev.borsing.imagetopaint.domain.filter;

public record ValueScaleFilterParams(int numberOfValues,
                                      long seed,
                                      int maxIterations) {

    /** A gray channel only has 256 distinct levels, so more clusters than that is meaningless. */
    private static final int MAX_NUMBER_OF_VALUES = 256;

    /** Generous upper bound on k-means iterations - well past where it would have converged. */
    private static final int MAX_ITERATIONS_LIMIT = 100;

    public ValueScaleFilterParams {
        if (numberOfValues < 1 || numberOfValues > MAX_NUMBER_OF_VALUES) {
            throw new IllegalArgumentException(
                    "numberOfValues must be between 1 and " + MAX_NUMBER_OF_VALUES);
        }
        if (maxIterations < 1 || maxIterations > MAX_ITERATIONS_LIMIT) {
            throw new IllegalArgumentException(
                    "maxIterations must be between 1 and " + MAX_ITERATIONS_LIMIT);
        }
    }

    public ValueScaleFilterParams(int numberOfValues) {
        this(numberOfValues, 42, 10);
    }
}
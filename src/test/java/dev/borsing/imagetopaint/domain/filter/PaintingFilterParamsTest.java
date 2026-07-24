package dev.borsing.imagetopaint.domain.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PaintingFilterParamsTest {

    @Test
    void rejectsZeroOrNegativeNumberOfColors() {
        assertThrows(IllegalArgumentException.class, () -> new PaintingFilterParams(0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new PaintingFilterParams(-1, 1, 1));
    }

    @Test
    void rejectsExcessiveNumberOfColors() {
        assertThrows(IllegalArgumentException.class, () -> new PaintingFilterParams(1001, 1, 1));
    }

    @Test
    void rejectsZeroOrNegativeMaxIterations() {
        assertThrows(IllegalArgumentException.class, () -> new PaintingFilterParams(1, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new PaintingFilterParams(1, 1, -1));
    }

    @Test
    void rejectsExcessiveMaxIterations() {
        assertThrows(IllegalArgumentException.class, () -> new PaintingFilterParams(1, 1, 101));
    }
}
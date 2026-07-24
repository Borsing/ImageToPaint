package dev.borsing.imagetopaint.domain.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ValueScaleFilterParamsTest {

    @Test
    void rejectsZeroOrNegativeNumberOfValues() {
        assertThrows(IllegalArgumentException.class, () -> new ValueScaleFilterParams(0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new ValueScaleFilterParams(-1, 1, 1));
    }

    @Test
    void rejectsNumberOfValuesAboveTheGrayChannelRange() {
        assertThrows(IllegalArgumentException.class, () -> new ValueScaleFilterParams(257, 1, 1));
    }

    @Test
    void rejectsZeroOrNegativeMaxIterations() {
        assertThrows(IllegalArgumentException.class, () -> new ValueScaleFilterParams(1, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new ValueScaleFilterParams(1, 1, -1));
    }

    @Test
    void rejectsExcessiveMaxIterations() {
        assertThrows(IllegalArgumentException.class, () -> new ValueScaleFilterParams(1, 1, 101));
    }
}
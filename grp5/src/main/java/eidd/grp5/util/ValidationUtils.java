package eidd.grp5.util;

import java.util.Objects;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public static int requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must be >= 0");
        }
        return value;
    }

    public static <T> T requireNonNull(T value, String message) {
        return Objects.requireNonNull(value, message);
    }
}

package commons.kit.MathUtils;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * Utilities for bulletproof numeric operations using BigDecimal.
 *
 * <p>This class eliminates common pitfalls in numeric computation:</p>
 * <ul>
 * <li>Null-safe operations (nulls treated as ZERO or ONE depending on context)</li>
 * <li>Smart parsing of currency strings ($1,234.56 → 1234.56)</li>
 * <li>Division by zero protection (returns ZERO instead of throwing)</li>
 * <li>Locale-aware number parsing (handles both 1,234.56 and 1.234,56)</li>
 * </ul>
 *
 * <p><strong>Design Philosophy:</strong> Never throw exceptions. Always return
 * a sensible default (ZERO, false, etc.) to keep code flowing.</p>
 *
 */
public final class NumberUtils {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;

    // Private constructor to prevent instantiation
    private NumberUtils() {
        throw new AssertionError("No NumberUtils instances for you!");
    }

    // ========== Factory Methods ==========

    /**
     * Creates a BigDecimal from any object in a bulletproof manner.
     *
     * <p><strong>Conversion Rules:</strong></p>
     * <ul>
     * <li>null → BigDecimal.ZERO</li>
     * <li>"" (empty string) → BigDecimal.ZERO</li>
     * <li>"$1,234.56" → 1234.56 (removes currency symbols and thousands separators)</li>
     * <li>"1.234,56" → 1234.56 (handles European format)</li>
     * <li>Number types → direct conversion</li>
     * </ul>
     *
     * @param value the value to convert
     * @return BigDecimal representation, never null (defaults to ZERO)
     */
    public static BigDecimal safeOf(Object value) {
        if (value == null) {
            return ZERO;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }

        String str = value.toString().trim();
        if (str.isEmpty()) {
            return ZERO;
        }

        // Clean currency symbols and whitespace
        str = str.replaceAll("[\\$€£¥\\s]", "");

        // Detect format: if contains both comma and dot, determine which is decimal separator
        if (str.contains(",") && str.contains(".")) {
            int lastComma = str.lastIndexOf(',');
            int lastDot = str.lastIndexOf('.');

            // If dot comes after comma, it's decimal separator (1,234.56)
            if (lastDot > lastComma) {
                str = str.replace(",", "");
            } else {
                // Comma is decimal separator (1.234,56 → 1234.56)
                str = str.replace(".", "").replace(",", ".");
            }
        } else if (str.contains(",")) {
            // Only comma: assume decimal separator if less than 3 digits after it
            int commaPos = str.lastIndexOf(',');
            int digitsAfterComma = str.length() - commaPos - 1;

            if (digitsAfterComma <= 2) {
                str = str.replace(",", ".");
            } else {
                str = str.replace(",", "");
            }
        }

        try {
            return new BigDecimal(str);
        } catch (NumberFormatException e) {
            return ZERO;
        }
    }

    // ========== Comparison Methods ==========

    /**
     * Checks if value is positive (> 0).
     * Null-safe: returns false for null.
     *
     * @param value the value to check
     * @return true if value > 0
     */
    public static boolean isPos(BigDecimal value) {
        return value != null && value.compareTo(ZERO) > 0;
    }

    /**
     * Checks if value is negative (< 0).
     * Null-safe: returns false for null.
     *
     * @param value the value to check
     * @return true if value < 0
     */
    public static boolean isNeg(BigDecimal value) {
        return value != null && value.compareTo(ZERO) < 0;
    }

    /**
     * Checks if value is zero (ignoring scale).
     * Null-safe: returns true for null (treating null as zero).
     *
     * @param value the value to check
     * @return true if value equals zero
     */
    public static boolean isZero(BigDecimal value) {
        return value == null || value.compareTo(ZERO) == 0;
    }

    /**
     * Checks if two BigDecimals are numerically equal (ignoring scale).
     * Example: 1.0 equals 1.00
     *
     * @param a first value
     * @param b second value
     * @return true if numerically equal
     */
    public static boolean isEq(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.compareTo(b) == 0;
    }

    /**
     * Checks if a > b.
     *
     * @param a first value
     * @param b second value
     * @return true if a > b
     */
    public static boolean isGt(BigDecimal a, BigDecimal b) {
        return safeOf(a).compareTo(safeOf(b)) > 0;
    }

    /**
     * Checks if a >= b.
     *
     * @param a first value
     * @param b second value
     * @return true if a >= b
     */
    public static boolean isGte(BigDecimal a, BigDecimal b) {
        return safeOf(a).compareTo(safeOf(b)) >= 0;
    }

    /**
     * Checks if value is within the inclusive range [min, max].
     *
     * @param value the value to check
     * @param min minimum bound
     * @param max maximum bound
     * @return true if min <= value <= max
     */
    public static boolean inRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        BigDecimal val = safeOf(value);
        return val.compareTo(safeOf(min)) >= 0 && val.compareTo(safeOf(max)) <= 0;
    }

    // ========== Arithmetic Operations ==========

    /**
     * Adds multiple BigDecimal values.
     * Treats null as ZERO.
     *
     * @param values values to add
     * @return sum of all values
     */
    public static BigDecimal add(BigDecimal... values) {
        return Arrays.stream(values)
                .map(NumberUtils::safeOf)
                .reduce(ZERO, BigDecimal::add);
    }

    /**
     * Subtracts values sequentially: values[0] - values[1] - values[2] - ...
     * Treats null as ZERO.
     *
     * @param values values to subtract
     * @return result of sequential subtraction
     */
    public static BigDecimal sub(BigDecimal... values) {
        if (values.length == 0) return ZERO;

        BigDecimal result = safeOf(values[0]);
        for (int i = 1; i < values.length; i++) {
            result = result.subtract(safeOf(values[i]));
        }
        return result;
    }

    /**
     * Multiplies multiple BigDecimal values.
     * Treats null as ZERO (any null value makes result ZERO).
     *
     * @param values values to multiply
     * @return product of all values
     */
    public static BigDecimal mul(BigDecimal... values) {
        if (values.length == 0) return ZERO;

        BigDecimal result = ONE;
        for (BigDecimal value : values) {
            if (value == null) return ZERO; // Null annihilates product
            result = result.multiply(value);
        }
        return result;
    }

    /**
     * Divides a by b with default rounding.
     *
     * <p><strong>Protection:</strong> Returns ZERO if b is zero (no exception thrown).</p>
     *
     * @param a dividend
     * @param b divisor
     * @return a / b, or ZERO if b is zero
     */
    public static BigDecimal div(BigDecimal a, BigDecimal b) {
        BigDecimal dividend = safeOf(a);
        BigDecimal divisor = safeOf(b);

        if (isZero(divisor)) {
            return ZERO; // Avoid division by zero
        }

        return dividend.divide(divisor, DEFAULT_SCALE, DEFAULT_ROUNDING);
    }

    /**
     * Finds the minimum value among the given values.
     * Ignores null values.
     *
     * @param values values to compare
     * @return minimum value, or ZERO if all values are null
     */
    public static BigDecimal min(BigDecimal... values) {
        return Arrays.stream(values)
                .filter(v -> v != null)
                .min(BigDecimal::compareTo)
                .orElse(ZERO);
    }

    /**
     * Finds the maximum value among the given values.
     * Ignores null values.
     *
     * @param values values to compare
     * @return maximum value, or ZERO if all values are null
     */
    public static BigDecimal max(BigDecimal... values) {
        return Arrays.stream(values)
                .filter(v -> v != null)
                .max(BigDecimal::compareTo)
                .orElse(ZERO);
    }

    /**
     * Clamps a value between min and max.
     *
     * @param value the value to clamp
     * @param min minimum bound
     * @param max maximum bound
     * @return min if value < min, max if value > max, otherwise value
     */
    public static BigDecimal clamp(BigDecimal value, BigDecimal min, BigDecimal max) {
        BigDecimal val = safeOf(value);
        BigDecimal minVal = safeOf(min);
        BigDecimal maxVal = safeOf(max);

        if (val.compareTo(minVal) < 0) return minVal;
        if (val.compareTo(maxVal) > 0) return maxVal;
        return val;
    }

    // ========== Utility Methods ==========

    /**
     * Rounds a BigDecimal to the specified number of decimal places.
     * Uses HALF_UP rounding mode (Standard rounding).
     * Null-safe: returns ZERO for null.
     *
     * @param value the value to round
     * @param decimals number of decimal places
     * @return rounded value
     */
    public static BigDecimal round(BigDecimal value, int decimals) {
        if (value == null) return ZERO;
        return value.setScale(decimals, DEFAULT_ROUNDING);
    }

    /**
     * Rounds a BigDecimal AWAY from zero (RoundingMode.UP).
     * Example: 1.1 -> 2, -1.1 -> -2 (if decimals=0)
     *
     * @param value the value to round
     * @param decimals number of decimal places
     * @return rounded value
     */
    public static BigDecimal roundUp(BigDecimal value, int decimals) {
        if (value == null) return ZERO;
        return value.setScale(decimals, RoundingMode.UP);
    }

    /**
     * Rounds a BigDecimal TOWARDS zero (RoundingMode.DOWN / Truncate).
     * Example: 1.9 -> 1, -1.9 -> -1 (if decimals=0)
     *
     * @param value the value to round
     * @param decimals number of decimal places
     * @return rounded value
     */
    public static BigDecimal roundDown(BigDecimal value, int decimals) {
        if (value == null) return ZERO;
        return value.setScale(decimals, RoundingMode.DOWN);
    }

    /**
     * Calculates a percentage of a base value.
     * Formula: base * (percent / 100)
     *
     * <p><strong>Example:</strong> percentage(200, 10) → 20</p>
     *
     * @param base the base value
     * @param percent the percentage (10 means 10%)
     * @return the calculated percentage amount
     */
    public static BigDecimal percentage(BigDecimal base, BigDecimal percent) {
        BigDecimal b = safeOf(base);
        BigDecimal p = safeOf(percent);

        // Optimization & Fix: If base is effectively zero, return constant ZERO.
        // This ensures the result has scale 0, satisfying strict equality tests against BigDecimal.ZERO.
        if (isZero(b)) {
            return ZERO;
        }

        return b.multiply(p).divide(new BigDecimal("100"), DEFAULT_SCALE, DEFAULT_ROUNDING);
    }

    /**
     * Extracts only digits (0-9) from a string.
     * Useful for cleaning phone numbers, IDs, etc.
     *
     * <p><strong>Example:</strong> extractDigits("(555) 123-4567") → "5551234567"</p>
     *
     * @param text the input text
     * @return string containing only digits, or empty string if input is null
     */
    public static String extractDigits(String text) {
        if (text == null) return "";
        return text.replaceAll("\\D", "");
    }
}
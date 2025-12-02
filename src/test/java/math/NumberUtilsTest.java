package math;


import commons.kit.MathUtils.NumberUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static commons.kit.MathUtils.NumberUtils.roundDown;
import static commons.kit.MathUtils.NumberUtils.roundUp;
import static org.junit.jupiter.api.Assertions.*;

class NumberUtilsTest {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;

    // ========================================================================
    // FACTORY METHOD TESTS - safeOf()
    // ========================================================================

    @Test
    @DisplayName("safeOf() - Returns ZERO for null")
    void testSafeOfNull() {
        BigDecimal result = NumberUtils.safeOf(null);

        assertEquals(ZERO, result);
    }

    @Test
    @DisplayName("safeOf() - Returns ZERO for empty string")
    void testSafeOfEmptyString() {
        BigDecimal result = NumberUtils.safeOf("");

        assertEquals(ZERO, result);
    }

    @Test
    @DisplayName("safeOf() - Returns ZERO for whitespace string")
    void testSafeOfWhitespace() {
        BigDecimal result = NumberUtils.safeOf("   ");

        assertEquals(ZERO, result);
    }

    @Test
    @DisplayName("safeOf() - Parses simple number")
    void testSafeOfSimpleNumber() {
        BigDecimal result = NumberUtils.safeOf("123.45");

        assertEquals(new BigDecimal("123.45"), result);
    }

    @Test
    @DisplayName("safeOf() - Parses negative number")
    void testSafeOfNegativeNumber() {
        BigDecimal result = NumberUtils.safeOf("-123.45");

        assertEquals(new BigDecimal("-123.45"), result);
    }

    @Test
    @DisplayName("safeOf() - Removes dollar sign")
    void testSafeOfDollarSign() {
        BigDecimal result = NumberUtils.safeOf("$1,234.56");

        assertEquals(new BigDecimal("1234.56"), result);
    }

    @Test
    @DisplayName("safeOf() - Removes euro sign")
    void testSafeOfEuroSign() {
        BigDecimal result = NumberUtils.safeOf("1.234,56 €");

        assertEquals(new BigDecimal("1234.56"), result);
    }

    @Test
    @DisplayName("safeOf() - Removes pound sign")
    void testSafeOfPoundSign() {
        BigDecimal result = NumberUtils.safeOf("£1,234.56");

        assertEquals(new BigDecimal("1234.56"), result);
    }

    @Test
    @DisplayName("safeOf() - Handles US format (1,234.56)")
    void testSafeOfUSFormat() {
        BigDecimal result = NumberUtils.safeOf("1,234.56");

        assertEquals(new BigDecimal("1234.56"), result);
    }

    @Test
    @DisplayName("safeOf() - Handles European format (1.234,56)")
    void testSafeOfEuropeanFormat() {
        BigDecimal result = NumberUtils.safeOf("1.234,56");

        assertEquals(new BigDecimal("1234.56"), result);
    }

    @Test
    @DisplayName("safeOf() - Handles comma as decimal separator")
    void testSafeOfCommaDecimal() {
        BigDecimal result = NumberUtils.safeOf("123,45");

        assertEquals(new BigDecimal("123.45"), result);
    }

    @Test
    @DisplayName("safeOf() - Handles comma as thousands separator")
    void testSafeOfCommaThousands() {
        BigDecimal result = NumberUtils.safeOf("1,234,567");

        assertEquals(new BigDecimal("1234567"), result);
    }

    @Test
    @DisplayName("safeOf() - Converts Integer")
    void testSafeOfInteger() {
        BigDecimal result = NumberUtils.safeOf(123);

        assertEquals(new BigDecimal("123"), result);
    }

    @Test
    @DisplayName("safeOf() - Converts Long")
    void testSafeOfLong() {
        BigDecimal result = NumberUtils.safeOf(123456789L);

        assertEquals(new BigDecimal("123456789"), result);
    }

    @Test
    @DisplayName("safeOf() - Converts Double")
    void testSafeOfDouble() {
        BigDecimal result = NumberUtils.safeOf(123.45);

        assertEquals(new BigDecimal("123.45"), result);
    }

    @Test
    @DisplayName("safeOf() - Returns BigDecimal unchanged")
    void testSafeOfBigDecimal() {
        BigDecimal original = new BigDecimal("123.45");
        BigDecimal result = NumberUtils.safeOf(original);

        assertSame(original, result);
    }

    @Test
    @DisplayName("safeOf() - Returns ZERO for invalid input")
    void testSafeOfInvalidInput() {
        BigDecimal result = NumberUtils.safeOf("not-a-number");

        assertEquals(ZERO, result);
    }

    // ========================================================================
    // COMPARISON TESTS
    // ========================================================================

    @Test
    @DisplayName("isPos() - Returns true for positive number")
    void testIsPosPositive() {
        assertTrue(NumberUtils.isPos(new BigDecimal("100")));
    }

    @Test
    @DisplayName("isPos() - Returns false for zero")
    void testIsPosZero() {
        assertFalse(NumberUtils.isPos(ZERO));
    }

    @Test
    @DisplayName("isPos() - Returns false for negative")
    void testIsPosNegative() {
        assertFalse(NumberUtils.isPos(new BigDecimal("-100")));
    }

    @Test
    @DisplayName("isPos() - Returns false for null")
    void testIsPosNull() {
        assertFalse(NumberUtils.isPos(null));
    }

    @Test
    @DisplayName("isNeg() - Returns true for negative number")
    void testIsNegNegative() {
        assertTrue(NumberUtils.isNeg(new BigDecimal("-100")));
    }

    @Test
    @DisplayName("isNeg() - Returns false for zero")
    void testIsNegZero() {
        assertFalse(NumberUtils.isNeg(ZERO));
    }

    @Test
    @DisplayName("isNeg() - Returns false for positive")
    void testIsNegPositive() {
        assertFalse(NumberUtils.isNeg(new BigDecimal("100")));
    }

    @Test
    @DisplayName("isNeg() - Returns false for null")
    void testIsNegNull() {
        assertFalse(NumberUtils.isNeg(null));
    }

    @Test
    @DisplayName("isZero() - Returns true for zero")
    void testIsZeroZero() {
        assertTrue(NumberUtils.isZero(ZERO));
    }

    @Test
    @DisplayName("isZero() - Returns true for null")
    void testIsZeroNull() {
        assertTrue(NumberUtils.isZero(null));
    }

    @Test
    @DisplayName("isZero() - Returns false for positive")
    void testIsZeroPositive() {
        assertFalse(NumberUtils.isZero(new BigDecimal("100")));
    }

    @Test
    @DisplayName("isZero() - Ignores scale (1.0 equals 1.00)")
    void testIsZeroDifferentScale() {
        assertTrue(NumberUtils.isZero(new BigDecimal("0.0")));
        assertTrue(NumberUtils.isZero(new BigDecimal("0.00")));
    }

    @Test
    @DisplayName("isEq() - Returns true for equal values")
    void testIsEqEqual() {
        assertTrue(NumberUtils.isEq(
                new BigDecimal("100"),
                new BigDecimal("100")
        ));
    }

    @Test
    @DisplayName("isEq() - Ignores scale (1.0 equals 1.00)")
    void testIsEqDifferentScale() {
        assertTrue(NumberUtils.isEq(
                new BigDecimal("1.0"),
                new BigDecimal("1.00")
        ));
    }

    @Test
    @DisplayName("isEq() - Returns true for both null")
    void testIsEqBothNull() {
        assertTrue(NumberUtils.isEq(null, null));
    }

    @Test
    @DisplayName("isEq() - Returns false for one null")
    void testIsEqOneNull() {
        assertFalse(NumberUtils.isEq(null, new BigDecimal("100")));
        assertFalse(NumberUtils.isEq(new BigDecimal("100"), null));
    }

    @Test
    @DisplayName("isGt() - Returns true when a > b")
    void testIsGtGreater() {
        assertTrue(NumberUtils.isGt(
                new BigDecimal("200"),
                new BigDecimal("100")
        ));
    }

    @Test
    @DisplayName("isGt() - Returns false when a equals b")
    void testIsGtEqual() {
        assertFalse(NumberUtils.isGt(
                new BigDecimal("100"),
                new BigDecimal("100")
        ));
    }

    @Test
    @DisplayName("isGt() - Returns false when a < b")
    void testIsGtLess() {
        assertFalse(NumberUtils.isGt(
                new BigDecimal("50"),
                new BigDecimal("100")
        ));
    }

    @Test
    @DisplayName("isGte() - Returns true when a > b")
    void testIsGteGreater() {
        assertTrue(NumberUtils.isGte(
                new BigDecimal("200"),
                new BigDecimal("100")
        ));
    }

    @Test
    @DisplayName("isGte() - Returns true when a equals b")
    void testIsGteEqual() {
        assertTrue(NumberUtils.isGte(
                new BigDecimal("100"),
                new BigDecimal("100")
        ));
    }

    @Test
    @DisplayName("inRange() - Returns true when in range")
    void testInRangeInside() {
        assertTrue(NumberUtils.inRange(
                new BigDecimal("50"),
                new BigDecimal("0"),
                new BigDecimal("100")
        ));
    }

    @Test
    @DisplayName("inRange() - Returns true at boundaries")
    void testInRangeBoundaries() {
        assertTrue(NumberUtils.inRange(
                new BigDecimal("0"),
                new BigDecimal("0"),
                new BigDecimal("100")
        ));
        assertTrue(NumberUtils.inRange(
                new BigDecimal("100"),
                new BigDecimal("0"),
                new BigDecimal("100")
        ));
    }

    @Test
    @DisplayName("inRange() - Returns false when outside range")
    void testInRangeOutside() {
        assertFalse(NumberUtils.inRange(
                new BigDecimal("150"),
                new BigDecimal("0"),
                new BigDecimal("100")
        ));
    }

    // ========================================================================
    // ARITHMETIC OPERATIONS
    // ========================================================================

    @Test
    @DisplayName("add() - Adds multiple values")
    void testAddMultiple() {
        BigDecimal result = NumberUtils.add(
                new BigDecimal("10"),
                new BigDecimal("20"),
                new BigDecimal("30")
        );

        assertEquals(new BigDecimal("60"), result);
    }

    @Test
    @DisplayName("add() - Treats null as ZERO")
    void testAddWithNull() {
        BigDecimal result = NumberUtils.add(
                new BigDecimal("10"),
                null,
                new BigDecimal("20")
        );

        assertEquals(new BigDecimal("30"), result);
    }

    @Test
    @DisplayName("add() - Returns ZERO for empty input")
    void testAddEmpty() {
        BigDecimal result = NumberUtils.add();

        assertEquals(ZERO, result);
    }

    @Test
    @DisplayName("sub() - Subtracts sequentially")
    void testSubSequential() {
        BigDecimal result = NumberUtils.sub(
                new BigDecimal("100"),
                new BigDecimal("30"),
                new BigDecimal("20")
        );

        assertEquals(new BigDecimal("50"), result);
    }

    @Test
    @DisplayName("sub() - Treats null as ZERO")
    void testSubWithNull() {
        BigDecimal result = NumberUtils.sub(
                new BigDecimal("100"),
                null,
                new BigDecimal("20")
        );

        assertEquals(new BigDecimal("80"), result);
    }

    @Test
    @DisplayName("sub() - Returns ZERO for empty input")
    void testSubEmpty() {
        BigDecimal result = NumberUtils.sub();

        assertEquals(ZERO, result);
    }

    @Test
    @DisplayName("mul() - Multiplies multiple values")
    void testMulMultiple() {
        BigDecimal result = NumberUtils.mul(
                new BigDecimal("2"),
                new BigDecimal("3"),
                new BigDecimal("4")
        );

        assertEquals(new BigDecimal("24"), result);
    }

    @Test
    @DisplayName("mul() - Null annihilates product")
    void testMulWithNull() {
        BigDecimal result = NumberUtils.mul(
                new BigDecimal("10"),
                null,
                new BigDecimal("20")
        );

        assertEquals(ZERO, result);
    }

    @Test
    @DisplayName("mul() - Returns ZERO for empty input")
    void testMulEmpty() {
        BigDecimal result = NumberUtils.mul();

        assertEquals(ZERO, result);
    }

    @Test
    @DisplayName("div() - Divides two numbers")
    void testDiv() {
        BigDecimal result = NumberUtils.div(
                new BigDecimal("100"),
                new BigDecimal("4")
        );

        assertEquals(new BigDecimal("25.00"), result);
    }

    @Test
    @DisplayName("div() - Returns ZERO when dividing by zero")
    void testDivByZero() {
        BigDecimal result = NumberUtils.div(
                new BigDecimal("100"),
                ZERO
        );

        assertEquals(ZERO, result); // No exception thrown!
    }

    @Test
    @DisplayName("div() - Handles null divisor as zero")
    void testDivNullDivisor() {
        BigDecimal result = NumberUtils.div(
                new BigDecimal("100"),
                null
        );

        assertEquals(ZERO, result);
    }

    @Test
    @DisplayName("div() - Rounds to 2 decimal places")
    void testDivRounding() {
        BigDecimal result = NumberUtils.div(
                new BigDecimal("10"),
                new BigDecimal("3")
        );

        // Should round to 2 decimals using HALF_UP
        assertEquals(new BigDecimal("3.33"), result);
    }

    @Test
    @DisplayName("min() - Finds minimum value")
    void testMin() {
        BigDecimal result = NumberUtils.min(
                new BigDecimal("50"),
                new BigDecimal("20"),
                new BigDecimal("80")
        );

        assertEquals(new BigDecimal("20"), result);
    }

    @Test
    @DisplayName("min() - Ignores null values")
    void testMinWithNull() {
        BigDecimal result = NumberUtils.min(
                new BigDecimal("50"),
                null,
                new BigDecimal("20")
        );

        assertEquals(new BigDecimal("20"), result);
    }

    @Test
    @DisplayName("min() - Returns ZERO for all nulls")
    void testMinAllNull() {
        BigDecimal result = NumberUtils.min(null, null);

        assertEquals(ZERO, result);
    }

    @Test
    @DisplayName("max() - Finds maximum value")
    void testMax() {
        BigDecimal result = NumberUtils.max(
                new BigDecimal("50"),
                new BigDecimal("80"),
                new BigDecimal("20")
        );

        assertEquals(new BigDecimal("80"), result);
    }

    @Test
    @DisplayName("max() - Ignores null values")
    void testMaxWithNull() {
        BigDecimal result = NumberUtils.max(
                new BigDecimal("50"),
                null,
                new BigDecimal("80")
        );

        assertEquals(new BigDecimal("80"), result);
    }

    @Test
    @DisplayName("clamp() - Clamps to minimum")
    void testClampMin() {
        BigDecimal result = NumberUtils.clamp(
                new BigDecimal("5"),
                new BigDecimal("10"),
                new BigDecimal("100")
        );

        assertEquals(new BigDecimal("10"), result);
    }

    @Test
    @DisplayName("clamp() - Clamps to maximum")
    void testClampMax() {
        BigDecimal result = NumberUtils.clamp(
                new BigDecimal("150"),
                new BigDecimal("10"),
                new BigDecimal("100")
        );

        assertEquals(new BigDecimal("100"), result);
    }

    @Test
    @DisplayName("clamp() - Returns value when in range")
    void testClampInRange() {
        BigDecimal result = NumberUtils.clamp(
                new BigDecimal("50"),
                new BigDecimal("10"),
                new BigDecimal("100")
        );

        assertEquals(new BigDecimal("50"), result);
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    @Test
    @DisplayName("round() - Rounds to specified decimals")
    void testRound() {
        BigDecimal result = NumberUtils.round(
                new BigDecimal("123.456789"),
                2
        );

        assertEquals(new BigDecimal("123.46"), result);
    }

    @Test
    @DisplayName("round() - Uses HALF_UP rounding")
    void testRoundHalfUp() {
        assertEquals(new BigDecimal("1.5"),
                NumberUtils.round(new BigDecimal("1.45"), 1));
        assertEquals(new BigDecimal("1.6"),
                NumberUtils.round(new BigDecimal("1.55"), 1));
    }

    @Test
    @DisplayName("round() - Returns ZERO for null")
    void testRoundNull() {
        BigDecimal result = NumberUtils.round(null, 2);

        assertEquals(ZERO, result);
    }

    @Test
    @DisplayName("percentage() - Calculates percentage")
    void testPercentage() {
        BigDecimal result = NumberUtils.percentage(
                new BigDecimal("200"),
                new BigDecimal("10")
        );

        assertEquals(new BigDecimal("20.00"), result);
    }

    @Test
    @DisplayName("percentage() - Handles decimals")
    void testPercentageDecimal() {
        BigDecimal result = NumberUtils.percentage(
                new BigDecimal("200"),
                new BigDecimal("7.5")
        );

        assertEquals(new BigDecimal("15.00"), result);
    }

    // ========================================================================
    // ROUND UP TESTS (RoundingMode.UP - Away from Zero)
    // ========================================================================

    @Test
    @DisplayName("roundUp() - Rounds positive numbers away from zero")
    void testRoundUpPositive() {
        // 1.1 -> 2
        assertEquals(new BigDecimal("2"), roundUp(new BigDecimal("1.1"), 0));
        // 1.0 -> 1 (no rounding needed)
        assertEquals(new BigDecimal("1"), roundUp(new BigDecimal("1.0"), 0));
        // 1.121 -> 1.13
        assertEquals(new BigDecimal("1.13"), roundUp(new BigDecimal("1.121"), 2));
    }

    @Test
    @DisplayName("roundUp() - Rounds negative numbers away from zero")
    void testRoundUpNegative() {
        // -1.1 -> -2
        assertEquals(new BigDecimal("-2"), roundUp(new BigDecimal("-1.1"), 0));
        // -1.121 -> -1.13
        assertEquals(new BigDecimal("-1.13"), roundUp(new BigDecimal("-1.121"), 2));
    }

    @Test
    @DisplayName("roundUp() - Handles null safely")
    void testRoundUpNull() {
        assertEquals(ZERO, roundUp(null, 2));
    }

    // ========================================================================
    // ROUND DOWN TESTS (RoundingMode.DOWN - Towards Zero / Truncate)
    // ========================================================================

    @Test
    @DisplayName("roundDown() - Rounds positive numbers towards zero")
    void testRoundDownPositive() {
        // 1.9 -> 1
        assertEquals(new BigDecimal("1"), roundDown(new BigDecimal("1.9"), 0));
        // 1.129 -> 1.12
        assertEquals(new BigDecimal("1.12"), roundDown(new BigDecimal("1.129"), 2));
    }

    @Test
    @DisplayName("roundDown() - Rounds negative numbers towards zero")
    void testRoundDownNegative() {
        // -1.9 -> -1
        assertEquals(new BigDecimal("-1"), roundDown(new BigDecimal("-1.9"), 0));
        // -1.129 -> -1.12
        assertEquals(new BigDecimal("-1.12"), roundDown(new BigDecimal("-1.129"), 2));
    }

    @Test
    @DisplayName("roundDown() - Handles null safely")
    void testRoundDownNull() {
        assertEquals(ZERO, roundDown(null, 2));
    }

    // ========================================================================
    // PERCENTAGE FIX TESTS
    // ========================================================================

    @Test
    @DisplayName("percentage() - Handles null base as exactly ZERO (scale 0)")
    void testPercentageNullBase() {
        // This previously failed because it returned 0.00 instead of 0
        BigDecimal result = NumberUtils.percentage(null, new BigDecimal("10"));
        assertEquals(ZERO, result);
        // Ensure scale is ignored or matches expected ZERO
        assertEquals(0, result.compareTo(ZERO));
    }

    @Test
    @DisplayName("percentage() - Handles zero base as exactly ZERO")
    void testPercentageZeroBase() {
        BigDecimal result = NumberUtils.percentage(BigDecimal.ZERO, new BigDecimal("10"));
        assertEquals(ZERO, result);
    }

    @Test
    @DisplayName("percentage() - Calculates correctly for non-zero")
    void testPercentageNormal() {
        // 10% of 200 = 20.00
        BigDecimal result = NumberUtils.percentage(new BigDecimal("200"), new BigDecimal("10"));
        assertTrue(NumberUtils.isEq(new BigDecimal("20"), result));
    }
    @Test
    @DisplayName("extractDigits() - Extracts digits from phone number")
    void testExtractDigitsPhone() {
        String result = NumberUtils.extractDigits("(555) 123-4567");

        assertEquals("5551234567", result);
    }

    @Test
    @DisplayName("extractDigits() - Removes all non-digits")
    void testExtractDigitsAllNonDigits() {
        String result = NumberUtils.extractDigits("ABC-123-DEF-456-GHI");

        assertEquals("123456", result);
    }

    @Test
    @DisplayName("extractDigits() - Returns empty for null")
    void testExtractDigitsNull() {
        String result = NumberUtils.extractDigits(null);

        assertEquals("", result);
    }

    @Test
    @DisplayName("extractDigits() - Returns empty for no digits")
    void testExtractDigitsNoDigits() {
        String result = NumberUtils.extractDigits("ABCDEF");

        assertEquals("", result);
    }

    // ========================================================================
    // EDGE CASES AND REAL-WORLD SCENARIOS
    // ========================================================================

    @Test
    @DisplayName("Real World - Calculate tax")
    void testRealWorldTax() {
        BigDecimal price = new BigDecimal("100.00");
        BigDecimal taxRate = new BigDecimal("15");

        BigDecimal tax = NumberUtils.percentage(price, taxRate);
        BigDecimal total = NumberUtils.add(price, tax);

        assertEquals(new BigDecimal("115.00"), total);
    }

    @Test
    @DisplayName("Real World - Calculate discount")
    void testRealWorldDiscount() {
        BigDecimal originalPrice = new BigDecimal("200.00");
        BigDecimal discountPercent = new BigDecimal("20");

        BigDecimal discount = NumberUtils.percentage(originalPrice, discountPercent);
        BigDecimal finalPrice = NumberUtils.sub(originalPrice, discount);

        assertEquals(new BigDecimal("160.00"), finalPrice);
    }

    @Test
    @DisplayName("Real World - Split bill")
    void testRealWorldSplitBill() {
        BigDecimal totalBill = new BigDecimal("125.50");
        BigDecimal people = new BigDecimal("4");

        BigDecimal perPerson = NumberUtils.div(totalBill, people);

        assertEquals(new BigDecimal("31.38"), perPerson); // Rounded to 2 decimals
    }

    @Test
    @DisplayName("Real World - Parse messy currency input")
    void testRealWorldMessyCurrency() {
        String userInput = "$ 1,234.56";
        BigDecimal amount = NumberUtils.safeOf(userInput);

        assertEquals(new BigDecimal("1234.56"), amount);
    }
}
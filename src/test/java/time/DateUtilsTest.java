package time;
import commons.kit.ErrorUtils.Result;
import commons.kit.TimeUtils.DateUtils;
import commons.kit.TimeUtils.ParsedDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for DateUtils.
 *
 * <p>Tests cover all date parsing, conversion, formatting, and business logic operations.</p>
 *
 * @author commons-kit
 * @version 1.3.1-SNAPSHOT
 */
class DateUtilsTest {

    // ========================================================================
    // SMART PARSING TESTS
    // ========================================================================

    @Test
    @DisplayName("analyze() - Parses ISO-8601 format successfully")
    void testAnalyzeISO8601() {
        Result<String, ParsedDate> result = DateUtils.analyze("2024-03-15");

        assertTrue(result.isOk());
        ParsedDate parsed = result.getOrThrow();
        assertEquals(LocalDate.of(2024, 3, 15), parsed.date());
        assertTrue(parsed.pattern().contains("yyyy-MM-dd"));
    }

    @Test
    @DisplayName("analyze() - Parses dd/MM/yyyy format")
    void testAnalyzeDDMMYYYY() {
        Result<String, ParsedDate> result = DateUtils.analyze("15/03/2024");

        assertTrue(result.isOk());
        ParsedDate parsed = result.getOrThrow();
        assertEquals(LocalDate.of(2024, 3, 15), parsed.date());
    }

    @Test
    @DisplayName("analyze() - Parses MM/dd/yyyy format")
    void testAnalyzeMMDDYYYY() {
        Result<String, ParsedDate> result = DateUtils.analyze("03/15/2024");

        assertTrue(result.isOk());
        ParsedDate parsed = result.getOrThrow();
        assertEquals(LocalDate.of(2024, 3, 15), parsed.date());
    }

    @Test
    @DisplayName("analyze() - Marks ambiguous patterns correctly")
    void testAnalyzeAmbiguousPattern() {
        Result<String, ParsedDate> result = DateUtils.analyze("01/02/2024");

        assertTrue(result.isOk());
        ParsedDate parsed = result.getOrThrow();
        // Could be Jan 2 or Feb 1 depending on pattern
        assertTrue(parsed.ambiguous());
    }

    @Test
    @DisplayName("analyze() - Returns error for null input")
    void testAnalyzeNullInput() {
        Result<String, ParsedDate> result = DateUtils.analyze(null);

        assertTrue(result.isErr());
        result.peekErr(error -> assertTrue(error.contains("null or empty")));
    }

    @Test
    @DisplayName("analyze() - Returns error for empty string")
    void testAnalyzeEmptyString() {
        Result<String, ParsedDate> result = DateUtils.analyze("   ");

        assertTrue(result.isErr());
    }

    @Test
    @DisplayName("analyze() - Returns error for unparseable format")
    void testAnalyzeInvalidFormat() {
        Result<String, ParsedDate> result = DateUtils.analyze("not-a-date");

        assertTrue(result.isErr());
        result.peekErr(error -> assertTrue(error.contains("Unable to parse")));
    }

    @Test
    @DisplayName("smartParse() - Returns LocalDate directly")
    void testSmartParse() {
        Result<String, LocalDate> result = DateUtils.smartParse("2024-03-15");

        assertTrue(result.isOk());
        assertEquals(LocalDate.of(2024, 3, 15), result.getOrThrow());
    }

    // ========================================================================
    // UNIVERSAL CONVERSION TESTS - toLocalDate()
    // ========================================================================

    @Test
    @DisplayName("toLocalDate() - Converts java.util.Date")
    void testToLocalDateFromUtilDate() {
        Date date = new Date(1710460800000L); // 2024-03-15 00:00:00 UTC
        Result<String, LocalDate> result = DateUtils.toLocalDate(date);

        assertTrue(result.isOk());
        assertNotNull(result.getOrThrow());
    }

    @Test
    @DisplayName("toLocalDate() - Converts java.sql.Date")
    void testToLocalDateFromSqlDate() {
        java.sql.Date sqlDate = java.sql.Date.valueOf("2024-03-15");
        Result<String, LocalDate> result = DateUtils.toLocalDate(sqlDate);

        assertTrue(result.isOk());
        assertEquals(LocalDate.of(2024, 3, 15), result.getOrThrow());
    }

    @Test
    @DisplayName("toLocalDate() - Converts Calendar")
    void testToLocalDateFromCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.MARCH, 15, 14, 30, 0);

        Result<String, LocalDate> result = DateUtils.toLocalDate(cal);

        assertTrue(result.isOk());
        assertEquals(LocalDate.of(2024, 3, 15), result.getOrThrow());
    }

    @Test
    @DisplayName("toLocalDate() - Converts Long timestamp")
    void testToLocalDateFromTimestamp() {
        long timestamp = 1710460800000L; // 2024-03-15 00:00:00 UTC
        Result<String, LocalDate> result = DateUtils.toLocalDate(timestamp);

        assertTrue(result.isOk());
        assertNotNull(result.getOrThrow());
    }

    @Test
    @DisplayName("toLocalDate() - Converts String")
    void testToLocalDateFromString() {
        Result<String, LocalDate> result = DateUtils.toLocalDate("2024-03-15");

        assertTrue(result.isOk());
        assertEquals(LocalDate.of(2024, 3, 15), result.getOrThrow());
    }

    @Test
    @DisplayName("toLocalDate() - Converts LocalDateTime (extracts date part)")
    void testToLocalDateFromLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2024, 3, 15, 14, 30);
        Result<String, LocalDate> result = DateUtils.toLocalDate(ldt);

        assertTrue(result.isOk());
        assertEquals(LocalDate.of(2024, 3, 15), result.getOrThrow());
    }

    @Test
    @DisplayName("toLocalDate() - Converts ZonedDateTime (extracts date part)")
    void testToLocalDateFromZonedDateTime() {
        ZonedDateTime zdt = ZonedDateTime.of(2024, 3, 15, 14, 30, 0, 0, ZoneId.of("UTC"));
        Result<String, LocalDate> result = DateUtils.toLocalDate(zdt);

        assertTrue(result.isOk());
        assertEquals(LocalDate.of(2024, 3, 15), result.getOrThrow());
    }

    @Test
    @DisplayName("toLocalDate() - Returns LocalDate unchanged")
    void testToLocalDateFromLocalDate() {
        LocalDate ld = LocalDate.of(2024, 3, 15);
        Result<String, LocalDate> result = DateUtils.toLocalDate(ld);

        assertTrue(result.isOk());
        assertEquals(ld, result.getOrThrow());
    }

    @Test
    @DisplayName("toLocalDate() - Returns error for null input")
    void testToLocalDateNullInput() {
        Result<String, LocalDate> result = DateUtils.toLocalDate(null);

        assertTrue(result.isErr());
        result.peekErr(error -> assertTrue(error.contains("null")));
    }

    @Test
    @DisplayName("toLocalDate() - Returns error for unsupported type")
    void testToLocalDateUnsupportedType() {
        Result<String, LocalDate> result = DateUtils.toLocalDate(new Object());

        assertTrue(result.isErr());
        result.peekErr(error -> assertTrue(error.contains("Unsupported type")));
    }

    // ========================================================================
    // UNIVERSAL CONVERSION TESTS - toLocalDateTime()
    // ========================================================================

    @Test
    @DisplayName("toLocalDateTime() - Converts java.util.Date with time")
    void testToLocalDateTimeFromUtilDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.MARCH, 15, 14, 30, 45);
        Date date = cal.getTime();

        Result<String, LocalDateTime> result = DateUtils.toLocalDateTime(date);

        assertTrue(result.isOk());
        LocalDateTime ldt = result.getOrThrow();
        assertEquals(15, ldt.getDayOfMonth());
        assertEquals(3, ldt.getMonthValue());
        assertEquals(2024, ldt.getYear());
    }

    @Test
    @DisplayName("toLocalDateTime() - Converts LocalDate (assumes 00:00:00)")
    void testToLocalDateTimeFromLocalDate() {
        LocalDate ld = LocalDate.of(2024, 3, 15);
        Result<String, LocalDateTime> result = DateUtils.toLocalDateTime(ld);

        assertTrue(result.isOk());
        LocalDateTime ldt = result.getOrThrow();
        assertEquals(LocalDateTime.of(2024, 3, 15, 0, 0, 0), ldt);
    }

    @Test
    @DisplayName("toLocalDateTime() - Returns LocalDateTime unchanged")
    void testToLocalDateTimeFromLocalDateTime() {
        LocalDateTime original = LocalDateTime.of(2024, 3, 15, 14, 30);
        Result<String, LocalDateTime> result = DateUtils.toLocalDateTime(original);

        assertTrue(result.isOk());
        assertEquals(original, result.getOrThrow());
    }

    @Test
    @DisplayName("toLocalDateTime() - Converts String (assumes 00:00:00)")
    void testToLocalDateTimeFromString() {
        Result<String, LocalDateTime> result = DateUtils.toLocalDateTime("2024-03-15");

        assertTrue(result.isOk());
        assertEquals(LocalDateTime.of(2024, 3, 15, 0, 0, 0), result.getOrThrow());
    }

    // ========================================================================
    // TIMEZONE CONVERSION TESTS
    // ========================================================================

    @Test
    @DisplayName("toZonedDateTime() - Converts to specified timezone")
    void testToZonedDateTime() {
        LocalDate ld = LocalDate.of(2024, 3, 15);
        ZoneId tokyo = ZoneId.of("Asia/Tokyo");

        Result<String, ZonedDateTime> result = DateUtils.toZonedDateTime(ld, tokyo);

        assertTrue(result.isOk());
        ZonedDateTime zdt = result.getOrThrow();
        assertEquals(tokyo, zdt.getZone());
        assertEquals(15, zdt.getDayOfMonth());
    }

    @Test
    @DisplayName("toUTC() - Converts to UTC timezone")
    void testToUTC() {
        LocalDate ld = LocalDate.of(2024, 3, 15);
        Result<String, ZonedDateTime> result = DateUtils.toUTC(ld);

        assertTrue(result.isOk());
        ZonedDateTime zdt = result.getOrThrow();
        assertEquals(ZoneId.of("UTC"), zdt.getZone());
    }

    // ========================================================================
    // FORMATTING TESTS
    // ========================================================================

    @Test
    @DisplayName("format() - Formats LocalDate with pattern")
    void testFormatLocalDate() {
        LocalDate ld = LocalDate.of(2024, 3, 15);
        String formatted = DateUtils.format(ld, "yyyy-MM-dd");

        assertEquals("2024-03-15", formatted);
    }

    @Test
    @DisplayName("format() - Formats LocalDateTime with pattern")
    void testFormatLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2024, 3, 15, 14, 30, 45);
        String formatted = DateUtils.format(ldt, "yyyy-MM-dd HH:mm:ss");

        assertEquals("2024-03-15 14:30:45", formatted);
    }

    @Test
    @DisplayName("format() - Returns empty string for null input")
    void testFormatNullInput() {
        String formatted = DateUtils.format(null, "yyyy-MM-dd");

        assertEquals("", formatted);
    }

    @Test
    @DisplayName("format() - Returns toString() for invalid pattern")
    void testFormatInvalidPattern() {
        LocalDate ld = LocalDate.of(2024, 3, 15);
        String formatted = DateUtils.format(ld, "invalid-pattern");

        // Should fallback to toString()
        assertNotNull(formatted);
        assertFalse(formatted.isEmpty());
    }

    // ========================================================================
    // TIME MANIPULATION TESTS
    // ========================================================================

    @Test
    @DisplayName("atStartOfDay() - Sets time to 00:00:00.000")
    void testAtStartOfDay() {
        LocalDate ld = LocalDate.of(2024, 3, 15);
        LocalDateTime result = DateUtils.atStartOfDay(ld);

        assertEquals(LocalDateTime.of(2024, 3, 15, 0, 0, 0, 0), result);
    }

    @Test
    @DisplayName("atStartOfDay() - Works with java.util.Date")
    void testAtStartOfDayWithUtilDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.MARCH, 15, 14, 30, 45);
        Date date = cal.getTime();

        LocalDateTime result = DateUtils.atStartOfDay(date);

        assertEquals(15, result.getDayOfMonth());
        assertEquals(3, result.getMonthValue());
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
    }

    @Test
    @DisplayName("atEndOfDay() - Sets time to 23:59:59.999")
    void testAtEndOfDay() {
        LocalDate ld = LocalDate.of(2024, 3, 15);
        LocalDateTime result = DateUtils.atEndOfDay(ld);

        assertEquals(LocalDateTime.of(2024, 3, 15, 23, 59, 59, 999_999_999), result);
    }

    @Test
    @DisplayName("withTime() - Combines date with time")
    void testWithTime() {
        LocalDate ld = LocalDate.of(2024, 3, 15);
        LocalDateTime result = DateUtils.withTime(ld, "14:30");

        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30), result);
    }

    @Test
    @DisplayName("withTime() - Handles time without minutes")
    void testWithTimeHourOnly() {
        LocalDate ld = LocalDate.of(2024, 3, 15);
        LocalDateTime result = DateUtils.withTime(ld, "14");

        // Should default minutes to 0
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 0), result);
    }

    @Test
    @DisplayName("withTime() - Handles invalid time format gracefully")
    void testWithTimeInvalidFormat() {
        LocalDate ld = LocalDate.of(2024, 3, 15);
        LocalDateTime result = DateUtils.withTime(ld, "invalid");

        // Should return start of day as fallback
        assertEquals(LocalDateTime.of(2024, 3, 15, 0, 0), result);
    }

    // ========================================================================
    // BUSINESS LOGIC TESTS
    // ========================================================================

    @Test
    @DisplayName("isWeekend() - Returns true for Saturday")
    void testIsWeekendSaturday() {
        LocalDate saturday = LocalDate.of(2024, 3, 16); // Saturday
        boolean result = DateUtils.isWeekend(saturday);

        assertTrue(result);
    }

    @Test
    @DisplayName("isWeekend() - Returns true for Sunday")
    void testIsWeekendSunday() {
        LocalDate sunday = LocalDate.of(2024, 3, 17); // Sunday
        boolean result = DateUtils.isWeekend(sunday);

        assertTrue(result);
    }

    @Test
    @DisplayName("isWeekend() - Returns false for weekday")
    void testIsWeekendMonday() {
        LocalDate monday = LocalDate.of(2024, 3, 18); // Monday
        boolean result = DateUtils.isWeekend(monday);

        assertFalse(result);
    }

    @Test
    @DisplayName("isBusinessDay() - Returns true for Monday")
    void testIsBusinessDayMonday() {
        LocalDate monday = LocalDate.of(2024, 3, 18); // Monday
        boolean result = DateUtils.isBusinessDay(monday);

        assertTrue(result);
    }

    @Test
    @DisplayName("isBusinessDay() - Returns true for Friday")
    void testIsBusinessDayFriday() {
        LocalDate friday = LocalDate.of(2024, 3, 15); // Friday
        boolean result = DateUtils.isBusinessDay(friday);

        assertTrue(result);
    }

    @Test
    @DisplayName("isBusinessDay() - Returns false for Saturday")
    void testIsBusinessDaySaturday() {
        LocalDate saturday = LocalDate.of(2024, 3, 16); // Saturday
        boolean result = DateUtils.isBusinessDay(saturday);

        assertFalse(result);
    }

    @Test
    @DisplayName("daysBetween() - Calculates days between two dates")
    void testDaysBetween() {
        LocalDate start = LocalDate.of(2024, 3, 1);
        LocalDate end = LocalDate.of(2024, 3, 15);

        long days = DateUtils.daysBetween(start, end);

        assertEquals(14, days);
    }

    @Test
    @DisplayName("daysBetween() - Returns absolute value")
    void testDaysBetweenAbsolute() {
        LocalDate start = LocalDate.of(2024, 3, 15);
        LocalDate end = LocalDate.of(2024, 3, 1);

        long days = DateUtils.daysBetween(start, end);

        assertEquals(14, days); // Same as reverse order
    }

    @Test
    @DisplayName("daysBetween() - Returns 0 for null input")
    void testDaysBetweenNullInput() {
        long days = DateUtils.daysBetween(null, LocalDate.now());

        assertEquals(0, days);
    }

    @Test
    @DisplayName("daysBetween() - Works with different date types")
    void testDaysBetweenMixedTypes() {
        String startStr = "2024-03-01";
        LocalDate end = LocalDate.of(2024, 3, 15);

        long days = DateUtils.daysBetween(startStr, end);

        assertEquals(14, days);
    }

    // ========================================================================
    // EDGE CASES AND ERROR HANDLING
    // ========================================================================

    @Test
    @DisplayName("Edge Case - Leap year parsing")
    void testLeapYearParsing() {
        Result<String, LocalDate> result = DateUtils.smartParse("2024-02-29");

        assertTrue(result.isOk());
        assertEquals(LocalDate.of(2024, 2, 29), result.getOrThrow());
    }

    @Test
    @DisplayName("Edge Case - Invalid leap year date fails")
    void testInvalidLeapYear() {
        Result<String, LocalDate> result = DateUtils.smartParse("2023-02-29");

        assertTrue(result.isErr());
    }

    @Test
    @DisplayName("Edge Case - Date at epoch")
    void testEpochDate() {
        Result<String, LocalDate> result = DateUtils.toLocalDate(0L);

        assertTrue(result.isOk());
        assertEquals(LocalDate.of(1970, 1, 1), result.getOrThrow());
    }
}

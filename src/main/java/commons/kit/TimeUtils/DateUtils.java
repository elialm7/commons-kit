package commons.kit.TimeUtils;

import commons.kit.ErrorUtils.Result;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;

/**
 * Utilities for robust date and time operations.
 *
 * <p>This class provides the "universal converter" for dates - it can convert
 * almost any date-related object to LocalDate, LocalDateTime, or ZonedDateTime.</p>
 *
 */
public final class DateUtils {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    private static final DateParser[] PARSERS = {
            // STRICT for ISO patterns (Fixes 2023-02-29 bug)
            // Note: DateParser handles the 'yyyy' -> 'uuuu' conversion internally for strict mode
            new DateParser("yyyy-MM-dd'T'HH:mm:ss", ResolverStyle.STRICT),
            new DateParser("yyyy-MM-dd", ResolverStyle.STRICT),

            // SMART for human patterns (Allows "1/1/2024")
            new DateParser("dd/MM/yyyy", ResolverStyle.SMART),
            new DateParser("MM/dd/yyyy", ResolverStyle.SMART),
            new DateParser("dd-MM-yyyy", ResolverStyle.SMART),
            new DateParser("yyyy/MM/dd", ResolverStyle.SMART),
            new DateParser("dd.MM.yyyy", ResolverStyle.SMART)
    };

    private DateUtils() {
        throw new AssertionError("No DateUtils instances for you!");
    }

    // ========== Parsing Methods ==========

    public static Result<String, ParsedDate> analyze(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return Result.err("Date string is null or empty");
        }

        String trimmed = dateStr.trim();

        for (DateParser parser : PARSERS) {
            try {
                LocalDate date = LocalDate.parse(trimmed, parser.formatter);
                boolean ambiguous = isAmbiguousPattern(parser.pattern);
                return Result.ok(new ParsedDate(date, parser.pattern, ambiguous));
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }

        return Result.err("Unable to parse date: " + dateStr);
    }

    public static Result<String, LocalDate> smartParse(String dateStr) {
        return analyze(dateStr).map(ParsedDate::date);
    }

    // ========== Universal Converters ==========

    public static Result<String, LocalDate> toLocalDate(Object obj) {
        if (obj == null) {
            return Result.err("Input object is null");
        }

        try {
            if (obj instanceof LocalDate) return Result.ok((LocalDate) obj);
            if (obj instanceof LocalDateTime) return Result.ok(((LocalDateTime) obj).toLocalDate());
            if (obj instanceof ZonedDateTime) return Result.ok(((ZonedDateTime) obj).toLocalDate());

            // CRITICAL: Must check sql.Date BEFORE util.Date
            if (obj instanceof java.sql.Date) {
                return Result.ok(((java.sql.Date) obj).toLocalDate());
            }

            if (obj instanceof Date) {
                return Result.ok(((Date) obj).toInstant()
                        .atZone(DEFAULT_ZONE)
                        .toLocalDate());
            }

            if (obj instanceof Calendar) {
                Calendar cal = (Calendar) obj;
                return Result.ok(LocalDate.of(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH)
                ));
            }

            if (obj instanceof Long) {
                return Result.ok(Instant.ofEpochMilli((Long) obj)
                        .atZone(UTC_ZONE)
                        .toLocalDate());
            }

            if (obj instanceof String) {
                return smartParse((String) obj);
            }

            return Result.err("Unsupported type: " + obj.getClass().getName());

        } catch (Exception e) {
            return Result.err("Conversion failed: " + e.getMessage());
        }
    }

    public static Result<String, LocalDateTime> toLocalDateTime(Object obj) {
        if (obj == null) {
            return Result.err("Input object is null");
        }

        try {
            if (obj instanceof LocalDateTime) return Result.ok((LocalDateTime) obj);
            if (obj instanceof ZonedDateTime) return Result.ok(((ZonedDateTime) obj).toLocalDateTime());

            // CRITICAL: Must check sql.Date BEFORE util.Date
            if (obj instanceof java.sql.Date) {
                return Result.ok(((java.sql.Date) obj).toLocalDate().atStartOfDay());
            }

            if (obj instanceof Date) {
                return Result.ok(((Date) obj).toInstant()
                        .atZone(DEFAULT_ZONE)
                        .toLocalDateTime());
            }

            if (obj instanceof Calendar) {
                return Result.ok(LocalDateTime.ofInstant(((Calendar) obj).toInstant(), DEFAULT_ZONE));
            }

            if (obj instanceof Long) {
                return Result.ok(LocalDateTime.ofInstant(Instant.ofEpochMilli((Long) obj), UTC_ZONE));
            }

            if (obj instanceof LocalDate) {
                return Result.ok(((LocalDate) obj).atStartOfDay());
            }

            if (obj instanceof String) {
                return toLocalDate(obj).map(LocalDate::atStartOfDay);
            }

            return Result.err("Unsupported type: " + obj.getClass().getName());

        } catch (Exception e) {
            return Result.err("Conversion failed: " + e.getMessage());
        }
    }

    public static Result<String, ZonedDateTime> toZonedDateTime(Object obj, ZoneId zone) {
        return toLocalDateTime(obj).map(ldt -> ldt.atZone(zone));
    }

    public static Result<String, ZonedDateTime> toUTC(Object obj) {
        return toZonedDateTime(obj, ZoneId.of("UTC"));
    }

    // ========== Formatting ==========

    public static String format(Temporal temporal, String pattern) {
        if (temporal == null) return "";
        try {
            return DateTimeFormatter.ofPattern(pattern).format(temporal);
        } catch (Exception e) {
            return temporal.toString();
        }
    }

    // ========== Time Manipulation ==========

    public static LocalDateTime atStartOfDay(Object date) {
        return toLocalDate(date)
                .map(LocalDate::atStartOfDay)
                .getOrElse(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS));
    }

    public static LocalDateTime atEndOfDay(Object date) {
        return toLocalDate(date)
                .map(ld -> ld.atTime(23, 59, 59, 999_999_999))
                .getOrElse(LocalDateTime.now().withHour(23).withMinute(59).withSecond(59));
    }

    public static LocalDateTime withTime(Object date, String time) {
        Result<String, LocalDate> dateResult = toLocalDate(date);
        if (dateResult.isErr()) return LocalDateTime.now();

        try {
            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            return dateResult.getOrThrow().atTime(hour, minute);
        } catch (Exception e) {
            return dateResult.getOrThrow().atStartOfDay();
        }
    }

    // ========== Business Logic ==========

    public static boolean isWeekend(Object date) {
        return toLocalDate(date)
                .map(ld -> {
                    DayOfWeek day = ld.getDayOfWeek();
                    return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
                })
                .getOrElse(false);
    }

    public static boolean isBusinessDay(Object date) {
        return !isWeekend(date);
    }

    public static long daysBetween(Object date1, Object date2) {
        Result<String, LocalDate> d1 = toLocalDate(date1);
        Result<String, LocalDate> d2 = toLocalDate(date2);

        if (d1.isErr() || d2.isErr()) return 0;
        return Math.abs(ChronoUnit.DAYS.between(d1.getOrThrow(), d2.getOrThrow()));
    }

    // ========== Helper Methods ==========

    private static class DateParser {
        final DateTimeFormatter formatter;
        final String pattern;

        DateParser(String pattern, ResolverStyle style) {
            this.pattern = pattern;

            // FIX: If style is STRICT, we MUST use 'u' (proleptic year) instead of 'y' (year of era).
            // 'y' fails validation in strict mode if era is missing.
            String parsePattern = style == ResolverStyle.STRICT ? pattern.replace("y", "u") : pattern;

            this.formatter = DateTimeFormatter.ofPattern(parsePattern)
                    .withResolverStyle(style);
        }
    }

    private static boolean isAmbiguousPattern(String pattern) {
        return pattern.contains("dd/MM") || pattern.contains("MM/dd");
    }
}
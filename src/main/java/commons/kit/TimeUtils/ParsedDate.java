package commons.kit.TimeUtils;

import java.time.LocalDate;

/**
 * Record containing the result of date parsing with metadata.
 *
 * <p>This record provides transparency about how a date was parsed,
 * which is useful for debugging and validation.</p>
 *
 * @param date the parsed LocalDate
 * @param pattern the pattern that successfully matched
 * @param ambiguous true if the pattern could be interpreted multiple ways
 *                  (e.g., dd/MM/yyyy vs MM/dd/yyyy)
 *
 * @author commons-kit
 * @version 1.0.0
 */
public record ParsedDate(
        LocalDate date,
        String pattern,
        boolean ambiguous
) {

    /**
     * Creates a ParsedDate with validation.
     *
     * @throws IllegalArgumentException if date or pattern is null
     */
    public ParsedDate {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be null or empty");
        }
    }

    /**
     * Returns a human-readable description of the parsing result.
     *
     * @return description string
     */
    public String describe() {
        String ambiguityNote = ambiguous ? " (ambiguous format)" : "";
        return String.format("Parsed as %s using pattern '%s'%s",
                date, pattern, ambiguityNote);
    }
}

package io.dd.test.vacation.util;

import io.dd.test.vacation.persistence.specification.SearchCriteria;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CriteriaParser {

    private static final Pattern CRITERIA_PATTERN = Pattern.compile("^(\\w+)([<>=~]+)(.+)$");

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Parses a criteria string like "createdAt>2026-01-13T23:56:51.797109"
     * into a SearchCriteria record.
     *
     * Supported operations: <, >, =, <=, >=, ~ (delimiters <>~=)
     * Value types attempted in order:
     * - Integer (if parseable as long)
     * - LocalDateTime (ISO format with time)
     * - LocalDate (ISO date only)
     * - fallback to String (trimmed)
     *
     * @param criteria the input criteria string
     * @return SearchCriteria with parsed key, operation, and value
     * @throws IllegalArgumentException if the format is invalid or value cannot be parsed
     */
    public static SearchCriteria parseCriteriaString(String criteria) {
        if (criteria == null || criteria.isBlank()) {
            throw new IllegalArgumentException("Criteria string cannot be null or empty");
        }

        Matcher matcher = CRITERIA_PATTERN.matcher(criteria.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid criteria format: " + criteria);
        }

        String key = matcher.group(1);
        String operation = matcher.group(2);
        String valueStr = matcher.group(3).trim();

        // Supported operations check (allows < > = <= >= ~ <> but <> not typical)
        if (!operation.matches("[<>=~]+") || operation.contains("<>") || operation.contains("><")) {
            throw new IllegalArgumentException("Unsupported operation: " + operation);
        }

        Object value = parseValue(valueStr);

        return new SearchCriteria(key, operation, value);
    }

    private static Object parseValue(String valueStr) {
        // Try Integer first
        try {
            return Long.parseLong(valueStr); // Use Long to cover int range safely; can be used as Integer if needed
        } catch (NumberFormatException ignored) {}

        // Try LocalDateTime (has 'T' and time)
        if (valueStr.contains("T")) {
            try {
                return LocalDateTime.parse(valueStr, DATE_TIME_FORMATTER);
            } catch (DateTimeParseException ignored) {}
        }

        // Try LocalDate
        try {
            return LocalDate.parse(valueStr, DATE_FORMATTER);
        } catch (DateTimeParseException ignored) {}

        // Fallback to String (trimmed)
        return valueStr;
    }

}
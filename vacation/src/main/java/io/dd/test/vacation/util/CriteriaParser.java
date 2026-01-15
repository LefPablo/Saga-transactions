package io.dd.test.vacation.util;

import io.dd.test.vacation.persistence.specification.SearchCriteria;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CriteriaParser {

    private static final Pattern CRITERIA_PATTERN = Pattern.compile("^(\\w+)([<>=~]+)(.+)$");

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

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

        Set<String> supportedOperations = Set.of("<", ">", "<>", "=", "<=", ">=", "~");
        if (!supportedOperations.contains(operation)) {
            throw new IllegalArgumentException("Unsupported operation: " + operation);
        }

        Object value = parseValue(valueStr);

        return new SearchCriteria(key, operation, value);
    }

    private static Object parseValue(String valueStr) {
        try {
            return Long.parseLong(valueStr);
        } catch (NumberFormatException ignored) {}

        try {
            return Double.parseDouble(valueStr);
        } catch (NumberFormatException ignored) {}

        // Try LocalDateTime (has 'T' and time)
        if (valueStr.contains("T")) {
            try {
                return LocalDateTime.parse(valueStr, DATE_TIME_FORMATTER);
            } catch (DateTimeParseException ignored) {}
        }

        try {
            return LocalDate.parse(valueStr, DATE_FORMATTER);
        } catch (DateTimeParseException ignored) {}

        return valueStr;
    }

}
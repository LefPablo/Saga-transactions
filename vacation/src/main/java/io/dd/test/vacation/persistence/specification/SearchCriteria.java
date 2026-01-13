package io.dd.test.vacation.persistence.specification;

public record SearchCriteria(String key, String operation, Object value) {
}
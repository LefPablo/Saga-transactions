package io.dd.test.vacation.persistence.specification;

import io.dd.test.vacation.persistence.model.VacationRequest;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class VacationRequestSpecification implements Specification<VacationRequest> {

    private final SearchCriteria criteria;

    @Override
    public Predicate toPredicate(Root<VacationRequest> root,
                                 CriteriaQuery<?> query,
                                 CriteriaBuilder builder) {

        Object value = criteria.value();
        String key = criteria.key();
        String op = criteria.operation();

        // Get the attribute path
        Path<?> path = root.get(key);

        // Handle comparison operations: >, >=, <, <=
        if (op.equals(">") || op.equals(">=") ||
                op.equals("<") || op.equals("<=")) {

            if (value instanceof LocalDateTime dateTime) {
                Path<LocalDateTime> dateTimePath = root.get(key);
                return switch (op) {
                    case ">"  -> builder.greaterThan(dateTimePath, dateTime);
                    case ">=" -> builder.greaterThanOrEqualTo(dateTimePath, dateTime);
                    case "<"  -> builder.lessThan(dateTimePath, dateTime);
                    case "<=" -> builder.lessThanOrEqualTo(dateTimePath, dateTime);
                    default -> null;
                };
            }

            if (value instanceof LocalDate date) {
                Path<LocalDate> datePath = root.get(key);
                return switch (op) {
                    case ">"  -> builder.greaterThan(datePath, date);
                    case ">=" -> builder.greaterThanOrEqualTo(datePath, date);
                    case "<"  -> builder.lessThan(datePath, date);
                    case "<=" -> builder.lessThanOrEqualTo(datePath, date);
                    default -> null;
                };
            }

            if (value instanceof Number number) {
                Path<Number> numberPath = root.get(key);
                return switch (op) {
                    case ">"  -> builder.gt(numberPath, number);
                    case ">=" -> builder.ge(numberPath, number);
                    case "<"  -> builder.lt(numberPath, number);
                    case "<=" -> builder.le(numberPath, number);
                    default -> null;
                };
            }
        }

        // Handle equality (=) - default for unsupported ops too
        if (op.equals("=") || op.equals("~")) {
            if (value instanceof String str && op.equals("~")) {
                // Like search for strings with ~ operator
                if (path.getJavaType() == String.class) {
                    return builder.like(
                            root.get(key),
                            "%" + str + "%");
                }
            }
            // Exact match for all types (including String with =)
            return builder.equal(root.get(key), value);
        }

        return null; // Unsupported operation
    }
}

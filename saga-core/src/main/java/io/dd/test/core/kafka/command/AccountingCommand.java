package io.dd.test.core.kafka.command;

import java.math.BigDecimal;

public record AccountingCommand(Long requestId, BigDecimal budget) {
}

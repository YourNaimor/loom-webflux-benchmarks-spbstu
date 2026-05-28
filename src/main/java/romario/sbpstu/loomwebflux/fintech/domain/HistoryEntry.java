package romario.sbpstu.loomwebflux.fintech.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record HistoryEntry(String transactionId, String userId, BigDecimal amount, String status, Instant timestamp) {}

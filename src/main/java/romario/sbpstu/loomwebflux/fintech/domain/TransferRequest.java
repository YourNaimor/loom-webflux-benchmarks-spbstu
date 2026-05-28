package romario.sbpstu.loomwebflux.fintech.domain;

import java.math.BigDecimal;

public record TransferRequest(String userId, BigDecimal amount, String toAccountId) {}

package romario.sbpstu.loomwebflux.fintech.domain;

import java.math.BigDecimal;

public record ProductInfo(String productId, String type, BigDecimal dailyLimit) {
    public static ProductInfo mock(String userId) {
        return new ProductInfo("PROD-" + userId, "DEBIT_CARD", BigDecimal.valueOf(100_000));
    }
}

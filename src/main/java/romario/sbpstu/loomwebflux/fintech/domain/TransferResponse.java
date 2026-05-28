package romario.sbpstu.loomwebflux.fintech.domain;

public record TransferResponse(String transactionId, String status, long timestamp) {
    public static TransferResponse success(String transactionId) {
        return new TransferResponse(transactionId, "COMPLETED", System.currentTimeMillis());
    }
}

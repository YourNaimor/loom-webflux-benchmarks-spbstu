package romario.sbpstu.loomwebflux.fintech.domain;

public record ClientInfo(String userId, String name, String tier) {
    public static ClientInfo mock(String userId) {
        return new ClientInfo(userId, "Client-" + userId, "STANDARD");
    }
}

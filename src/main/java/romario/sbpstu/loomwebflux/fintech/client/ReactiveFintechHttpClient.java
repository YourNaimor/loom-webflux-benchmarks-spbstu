package romario.sbpstu.loomwebflux.fintech.client;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import romario.sbpstu.loomwebflux.fintech.domain.ClientInfo;
import romario.sbpstu.loomwebflux.fintech.domain.HistoryEntry;
import romario.sbpstu.loomwebflux.fintech.domain.ProductInfo;
import romario.sbpstu.loomwebflux.fintech.domain.TransferResponse;

import java.util.List;
import java.util.UUID;

/**
 * Реактивный HTTP-клиент для webflux-netty реализации.
 * Вызовы к mock-сервисам защищены circuit breaker (Resilience4j + reactor operator).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReactiveFintechHttpClient {

    private static final String CB_DOWNSTREAM = "downstream";

    private final WebClient webClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public Mono<String> verifyAuth() {
        return webClient.get().uri("/mock/auth/reactive").retrieve().bodyToMono(String.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker(CB_DOWNSTREAM)))
                .onErrorReturn("FALLBACK");
    }

    public Mono<ClientInfo> getClient(String userId) {
        return webClient.get().uri("/mock/clients/" + userId + "/reactive").retrieve().bodyToMono(ClientInfo.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker(CB_DOWNSTREAM)))
                .onErrorResume(t -> {
                    log.warn("clients circuit open for userId={}: {}", userId, t.getMessage());
                    return Mono.just(ClientInfo.mock(userId));
                });
    }

    public Mono<ProductInfo> getProduct(String userId) {
        return webClient.get().uri("/mock/products/" + userId + "/reactive").retrieve().bodyToMono(ProductInfo.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker(CB_DOWNSTREAM)))
                .onErrorResume(t -> {
                    log.warn("products circuit open for userId={}: {}", userId, t.getMessage());
                    return Mono.just(ProductInfo.mock(userId));
                });
    }

    public Mono<List<HistoryEntry>> getHistory(String userId) {
        return webClient.get().uri("/mock/history/" + userId + "/reactive")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<HistoryEntry>>() {})
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker(CB_DOWNSTREAM)))
                .onErrorReturn(List.of());
    }

    public Mono<HistoryEntry> saveHistory(HistoryEntry entry) {
        return webClient.post().uri("/mock/history/reactive").bodyValue(entry).retrieve().bodyToMono(HistoryEntry.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker(CB_DOWNSTREAM)))
                .onErrorReturn(entry);
    }

    public Mono<TransferResponse> executePayment(String payload) {
        return webClient.post().uri("/mock/payment/reactive").bodyValue(payload).retrieve().bodyToMono(TransferResponse.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker(CB_DOWNSTREAM)))
                .onErrorReturn(new TransferResponse(UUID.randomUUID().toString(), "FALLBACK", System.currentTimeMillis()));
    }
}

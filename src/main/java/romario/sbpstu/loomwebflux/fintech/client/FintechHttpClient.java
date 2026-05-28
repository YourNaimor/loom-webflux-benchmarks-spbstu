package romario.sbpstu.loomwebflux.fintech.client;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import romario.sbpstu.loomwebflux.fintech.domain.ClientInfo;
import romario.sbpstu.loomwebflux.fintech.domain.HistoryEntry;
import romario.sbpstu.loomwebflux.fintech.domain.ProductInfo;
import romario.sbpstu.loomwebflux.fintech.domain.TransferResponse;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class FintechHttpClient {

    private static final String CB = "downstream";

    private final WebClient webClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public String verifyAuth() {
        return webClient.get().uri("/mock/auth").retrieve().bodyToMono(String.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker(CB)))
                .onErrorReturn("FALLBACK")
                .block();
    }

    public ClientInfo getClient(String userId) {
        return webClient.get().uri("/mock/clients/" + userId).retrieve().bodyToMono(ClientInfo.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker(CB)))
                .onErrorResume(t -> reactor.core.publisher.Mono.just(ClientInfo.mock(userId)))
                .block();
    }

    public ProductInfo getProduct(String userId) {
        return webClient.get().uri("/mock/products/" + userId).retrieve().bodyToMono(ProductInfo.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker(CB)))
                .onErrorResume(t -> reactor.core.publisher.Mono.just(ProductInfo.mock(userId)))
                .block();
    }

    public List<HistoryEntry> getHistory(String userId) {
        return webClient.get().uri("/mock/history/" + userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<HistoryEntry>>() {})
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker(CB)))
                .onErrorReturn(List.of())
                .block();
    }

    public HistoryEntry saveHistory(HistoryEntry entry) {
        return webClient.post().uri("/mock/history").bodyValue(entry).retrieve().bodyToMono(HistoryEntry.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker(CB)))
                .onErrorReturn(entry)
                .block();
    }

    public TransferResponse executePayment(String payload) {
        return webClient.post().uri("/mock/payment").bodyValue(payload).retrieve().bodyToMono(TransferResponse.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker(CB)))
                .onErrorReturn(new TransferResponse(UUID.randomUUID().toString(), "FALLBACK", System.currentTimeMillis()))
                .block();
    }
}

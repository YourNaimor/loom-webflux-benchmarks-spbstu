package romario.sbpstu.loomwebflux.fintech.mock;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import romario.sbpstu.loomwebflux.fintech.domain.ClientInfo;
import romario.sbpstu.loomwebflux.fintech.domain.HistoryEntry;
import romario.sbpstu.loomwebflux.fintech.domain.ProductInfo;
import romario.sbpstu.loomwebflux.fintech.domain.TransferResponse;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Имитирует downstream-сервисы с фиксированными задержками согласно дипломной работе:
 * auth ~10ms, clients ~30ms, products ~20ms, history ~60ms.
 * Используется обоими подходами (loom-tomcat и webflux-netty).
 */
@RestController
@RequestMapping("/mock")
public class MockServiceController {

    static final long AUTH_DELAY_MS = 10;
    static final long CLIENTS_DELAY_MS = 30;
    static final long PRODUCTS_DELAY_MS = 20;
    static final long HISTORY_DELAY_MS = 60;
    static final long PAYMENT_DELAY_MS = 50;

    // --- auth ---

    @GetMapping("/auth")
    public String authBlocking() throws InterruptedException {
        Thread.sleep(AUTH_DELAY_MS);
        return "OK";
    }

    @GetMapping("/auth/reactive")
    public Mono<String> authReactive() {
        return Mono.delay(Duration.ofMillis(AUTH_DELAY_MS)).thenReturn("OK");
    }

    // --- clients ---

    @GetMapping("/clients/{userId}")
    public ClientInfo clientBlocking(@PathVariable String userId) throws InterruptedException {
        Thread.sleep(CLIENTS_DELAY_MS);
        return ClientInfo.mock(userId);
    }

    @GetMapping("/clients/{userId}/reactive")
    public Mono<ClientInfo> clientReactive(@PathVariable String userId) {
        return Mono.delay(Duration.ofMillis(CLIENTS_DELAY_MS))
                .map(d -> ClientInfo.mock(userId));
    }

    // --- products ---

    @GetMapping("/products/{userId}")
    public ProductInfo productBlocking(@PathVariable String userId) throws InterruptedException {
        Thread.sleep(PRODUCTS_DELAY_MS);
        return ProductInfo.mock(userId);
    }

    @GetMapping("/products/{userId}/reactive")
    public Mono<ProductInfo> productReactive(@PathVariable String userId) {
        return Mono.delay(Duration.ofMillis(PRODUCTS_DELAY_MS))
                .map(d -> ProductInfo.mock(userId));
    }

    // --- history ---

    @GetMapping("/history/{userId}")
    public List<HistoryEntry> historyBlocking(@PathVariable String userId) throws InterruptedException {
        Thread.sleep(HISTORY_DELAY_MS);
        return List.of(new HistoryEntry(UUID.randomUUID().toString(), userId,
                BigDecimal.valueOf(1000), "COMPLETED", Instant.now()));
    }

    @GetMapping("/history/{userId}/reactive")
    public Mono<List<HistoryEntry>> historyReactive(@PathVariable String userId) {
        return Mono.delay(Duration.ofMillis(HISTORY_DELAY_MS))
                .map(d -> List.of(new HistoryEntry(UUID.randomUUID().toString(), userId,
                        BigDecimal.valueOf(1000), "COMPLETED", Instant.now())));
    }

    @PostMapping("/history")
    public HistoryEntry saveHistoryBlocking(@RequestBody HistoryEntry entry) throws InterruptedException {
        Thread.sleep(HISTORY_DELAY_MS);
        return entry;
    }

    @PostMapping("/history/reactive")
    public Mono<HistoryEntry> saveHistoryReactive(@RequestBody HistoryEntry entry) {
        return Mono.delay(Duration.ofMillis(HISTORY_DELAY_MS)).thenReturn(entry);
    }

    // --- payment (имитация внешнего платёжного API) ---

    @PostMapping("/payment")
    public TransferResponse paymentBlocking(@RequestBody String payload) throws InterruptedException {
        Thread.sleep(PAYMENT_DELAY_MS);
        return TransferResponse.success(UUID.randomUUID().toString());
    }

    @PostMapping("/payment/reactive")
    public Mono<TransferResponse> paymentReactive(@RequestBody String payload) {
        return Mono.delay(Duration.ofMillis(PAYMENT_DELAY_MS))
                .map(d -> TransferResponse.success(UUID.randomUUID().toString()));
    }
}

package romario.sbpstu.loomwebflux.fintech.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import romario.sbpstu.loomwebflux.fintech.client.FintechHttpClient;
import romario.sbpstu.loomwebflux.fintech.client.ReactiveFintechHttpClient;
import romario.sbpstu.loomwebflux.fintech.domain.HistoryEntry;

import java.util.List;

/**
 * Gateway-сервис: тонкий прокси, реализующий паттерн аутентифицированного проксирования.
 * Сценарий 1 (get-history): JWT-верификация через auth mock + запрос истории.
 * Два метода: блокирующий (loom-tomcat) и реактивный (webflux-netty).
 */
@Service
@RequiredArgsConstructor
public class GatewayService {

    private final FintechHttpClient blockingClient;
    private final ReactiveFintechHttpClient reactiveClient;

    /**
     * Блокирующая реализация для loom-tomcat.
     * Цепочка: auth (10ms) → history (60ms) = ~70ms суммарно.
     */
    public List<HistoryEntry> getHistory(String userId) {
        blockingClient.verifyAuth();
        return blockingClient.getHistory(userId);
    }

    /**
     * Реактивная реализация для webflux-netty.
     * Цепочка: auth (10ms) → history (60ms) через flatMap.
     */
    public Mono<List<HistoryEntry>> getHistoryReactive(String userId) {
        return reactiveClient.verifyAuth()
                .flatMap(token -> reactiveClient.getHistory(userId));
    }
}

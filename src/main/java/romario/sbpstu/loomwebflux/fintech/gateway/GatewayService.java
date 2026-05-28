package romario.sbpstu.loomwebflux.fintech.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import romario.sbpstu.loomwebflux.fintech.client.FintechHttpClient;
import romario.sbpstu.loomwebflux.fintech.client.ReactiveFintechHttpClient;
import romario.sbpstu.loomwebflux.fintech.domain.HistoryEntry;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GatewayService {

    private final FintechHttpClient blockingClient;
    private final ReactiveFintechHttpClient reactiveClient;

    public List<HistoryEntry> getHistory(String userId) {
        blockingClient.verifyAuth();
        return blockingClient.getHistory(userId);
    }

    public Mono<List<HistoryEntry>> getHistoryReactive(String userId) {
        return reactiveClient.verifyAuth()
                .flatMap(token -> reactiveClient.getHistory(userId));
    }
}

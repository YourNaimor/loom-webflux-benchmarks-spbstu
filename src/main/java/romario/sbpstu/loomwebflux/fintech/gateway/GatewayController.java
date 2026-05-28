package romario.sbpstu.loomwebflux.fintech.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import romario.sbpstu.loomwebflux.fintech.domain.HistoryEntry;

import java.util.List;

import static romario.sbpstu.loomwebflux.common.Approaches.LOOM_TOMCAT;
import static romario.sbpstu.loomwebflux.common.Approaches.WEBFLUX_NETTY;

@RestController
@RequiredArgsConstructor
public class GatewayController {

    private static final String API_PATH = "/gateway/history";

    private final GatewayService gatewayService;

    @GetMapping(LOOM_TOMCAT + API_PATH)
    public List<HistoryEntry> getHistoryLoom(
            @RequestHeader(value = "X-User-Id", defaultValue = "user-1") String userId) {
        return gatewayService.getHistory(userId);
    }

    @GetMapping(WEBFLUX_NETTY + API_PATH)
    public Mono<List<HistoryEntry>> getHistoryWebflux(
            @RequestHeader(value = "X-User-Id", defaultValue = "user-1") String userId) {
        return gatewayService.getHistoryReactive(userId);
    }
}

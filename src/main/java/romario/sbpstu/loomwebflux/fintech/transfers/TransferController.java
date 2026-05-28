package romario.sbpstu.loomwebflux.fintech.transfers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import romario.sbpstu.loomwebflux.fintech.domain.TransferRequest;
import romario.sbpstu.loomwebflux.fintech.domain.TransferResponse;

import static romario.sbpstu.loomwebflux.common.Approaches.LOOM_TOMCAT;
import static romario.sbpstu.loomwebflux.common.Approaches.WEBFLUX_NETTY;

@RestController
@RequiredArgsConstructor
public class TransferController {

    private static final String API_PATH = "/transfers/transfer";

    private final TransferService transferService;

    @PostMapping(LOOM_TOMCAT + API_PATH)
    public TransferResponse doTransferLoom(@RequestBody TransferRequest request) {
        return transferService.doTransfer(request);
    }

    @PostMapping(WEBFLUX_NETTY + API_PATH)
    public Mono<TransferResponse> doTransferWebflux(@RequestBody TransferRequest request) {
        return transferService.doTransferReactive(request);
    }
}

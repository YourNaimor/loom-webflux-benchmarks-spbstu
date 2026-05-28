package romario.sbpstu.loomwebflux.fintech.transfers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import romario.sbpstu.loomwebflux.fintech.balancer.HashRingLoadBalancer;
import romario.sbpstu.loomwebflux.fintech.cache.TwoLevelCache;
import romario.sbpstu.loomwebflux.fintech.client.FintechHttpClient;
import romario.sbpstu.loomwebflux.fintech.client.ReactiveFintechHttpClient;
import romario.sbpstu.loomwebflux.fintech.domain.ClientInfo;
import romario.sbpstu.loomwebflux.fintech.domain.HistoryEntry;
import romario.sbpstu.loomwebflux.fintech.domain.ProductInfo;
import romario.sbpstu.loomwebflux.fintech.domain.TransferRequest;
import romario.sbpstu.loomwebflux.fintech.domain.TransferResponse;
import romario.sbpstu.loomwebflux.fintech.entity.TransactionRecord;
import romario.sbpstu.loomwebflux.fintech.entity.TransactionRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final FintechHttpClient blockingClient;
    private final ReactiveFintechHttpClient reactiveClient;
    private final TwoLevelCache cache;
    private final HashRingLoadBalancer hashRing;
    private final TransactionRepository transactionRepository;

    public TransferResponse doTransfer(TransferRequest request) {
        String userId = request.userId();
        String txId = UUID.randomUUID().toString();

        log.debug("hash-ring node={} for userId={}", hashRing.selectNode(userId), userId);

        cache.get("client:" + userId, () -> blockingClient.getClient(userId));
        cache.get("product:" + userId, () -> blockingClient.getProduct(userId));

        blockingClient.verifyAuth();

        TransferResponse payment = blockingClient.executePayment(txId);

        blockingClient.saveHistory(new HistoryEntry(txId, userId, request.amount(), payment.status(), Instant.now()));

        transactionRepository.save(TransactionRecord.builder()
                .transactionId(txId)
                .userId(userId)
                .amount(request.amount())
                .toAccountId(request.toAccountId())
                .status(payment.status())
                .createdAt(Instant.now())
                .build());

        return TransferResponse.success(txId);
    }

    public Mono<TransferResponse> doTransferReactive(TransferRequest request) {
        String userId = request.userId();
        String txId = UUID.randomUUID().toString();

        Mono<ClientInfo> clientMono = cache.getReactive("client:" + userId, reactiveClient.getClient(userId));
        Mono<ProductInfo> productMono = cache.getReactive("product:" + userId, reactiveClient.getProduct(userId));

        return Mono.zip(clientMono, productMono)
                .flatMap(tuple -> reactiveClient.verifyAuth().thenReturn(tuple))
                .flatMap(tuple -> reactiveClient.executePayment(txId))
                .flatMap(payment -> {
                    HistoryEntry entry = new HistoryEntry(txId, userId, request.amount(), payment.status(), Instant.now());
                    return reactiveClient.saveHistory(entry)
                            .flatMap(saved -> Mono.fromCallable(() -> {
                                transactionRepository.save(TransactionRecord.builder()
                                        .transactionId(txId)
                                        .userId(userId)
                                        .amount(request.amount())
                                        .toAccountId(request.toAccountId())
                                        .status(payment.status())
                                        .createdAt(Instant.now())
                                        .build());
                                return TransferResponse.success(txId);
                            }).subscribeOn(Schedulers.boundedElastic()));
                });
    }
}

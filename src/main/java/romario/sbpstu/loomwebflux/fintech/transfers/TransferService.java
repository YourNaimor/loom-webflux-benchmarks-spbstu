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

/**
 * Сервис проведения платёжных транзакций (сценарий 2 диплома).
 * Реализует цепочку из 5 последовательных downstream-вызовов:
 *   1. GET clients (30ms) — с двухуровневым кешем
 *   2. GET products (20ms) — с двухуровневым кешем
 *   3. GET auth (10ms)    — повторная верификация
 *   4. POST payment API 1 (50ms) — через hash-ring балансировщик
 *   5. POST history (60ms) — сохранение результата
 * + запись в PostgreSQL через JPA
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final FintechHttpClient blockingClient;
    private final ReactiveFintechHttpClient reactiveClient;
    private final TwoLevelCache cache;
    private final HashRingLoadBalancer hashRing;
    private final TransactionRepository transactionRepository;

    /**
     * Блокирующая реализация для loom-tomcat.
     * Каждый I/O-вызов блокирует Virtual Thread; JVM автоматически
     * паркует его и освобождает carrier thread для других задач.
     */
    public TransferResponse doTransfer(TransferRequest request) {
        String userId = request.userId();
        String txId = UUID.randomUUID().toString();

        // hash-ring определяет детерминированный upstream для данного userId
        String selectedNode = hashRing.selectNode(userId);
        log.debug("hash-ring selected node={} for userId={}", selectedNode, userId);

        // 1. clients — с L1/L2 кешем
        String clientKey = "client:" + userId;
        cache.get(clientKey, () -> blockingClient.getClient(userId));

        // 2. products — с L1/L2 кешем
        String productKey = "product:" + userId;
        cache.get(productKey, () -> blockingClient.getProduct(userId));

        // 3. auth — повторная верификация
        blockingClient.verifyAuth();

        // 4. payment API — hash-ring выбирает upstream
        TransferResponse payment = blockingClient.executePayment(txId);

        // 5. save history
        HistoryEntry historyEntry = new HistoryEntry(txId, userId, request.amount(), payment.status(), Instant.now());
        blockingClient.saveHistory(historyEntry);

        // сохранение в PostgreSQL
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

    /**
     * Реактивная реализация для webflux-netty.
     * Цепочка flatMap обеспечивает неблокирующее выполнение всех 5 вызовов.
     * Благодаря Mono.zip() вызовы clients и products выполняются параллельно.
     */
    public Mono<TransferResponse> doTransferReactive(TransferRequest request) {
        String userId = request.userId();
        String txId = UUID.randomUUID().toString();

        // 1+2: clients и products — параллельно через Mono.zip + L1/L2 кеш
        String clientKey = "client:" + userId;
        String productKey = "product:" + userId;

        Mono<ClientInfo> clientMono = cache.getReactive(clientKey, reactiveClient.getClient(userId));
        Mono<ProductInfo> productMono = cache.getReactive(productKey, reactiveClient.getProduct(userId));

        return Mono.zip(clientMono, productMono)
                // 3. auth
                .flatMap(tuple -> reactiveClient.verifyAuth().thenReturn(tuple))
                // 4. payment
                .flatMap(tuple -> reactiveClient.executePayment(txId))
                // 5. save history + DB
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

package romario.sbpstu.loomwebflux.fintech.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Двухуровневый кеш: L1 (Caffeine, in-process, ~5ms) → L2 (Redis-симуляция, ~15ms) → source.
 * В тестовом стенде L2 реализован как отдельный ConcurrentHashMap с TTL-подобным поведением,
 * имитирующий задержку Redis (~15ms). Для production подключается Spring Data Redis.
 */
@Component
@Slf4j
public class TwoLevelCache {

    private static final Duration L1_TTL = Duration.ofMinutes(5);
    private static final Duration L2_TTL = Duration.ofMinutes(60);
    private static final long L2_SIMULATED_DELAY_MS = 15;

    private final Cache<String, Object> l1 = Caffeine.newBuilder()
            .expireAfterWrite(L1_TTL)
            .maximumSize(10_000)
            .build();

    // L2: симуляция Redis (distributed cache)
    private final Map<String, Object> l2 = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Supplier<T> source) {
        T value = (T) l1.getIfPresent(key);
        if (value != null) {
            return value;
        }
        // L1 miss → L2
        value = (T) l2.get(key);
        if (value != null) {
            // L2 hit: simulate read delay ~15ms (паркует Virtual Thread, не блокирует carrier)
            try { Thread.sleep(L2_SIMULATED_DELAY_MS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            l1.put(key, value);
            return value;
        }
        // L2 miss → source
        value = source.get();
        l1.put(key, value);
        l2.put(key, value);
        return value;
    }

    @SuppressWarnings("unchecked")
    public <T> Mono<T> getReactive(String key, Mono<T> source) {
        T l1Value = (T) l1.getIfPresent(key);
        if (l1Value != null) {
            return Mono.just(l1Value);
        }
        T l2Value = (T) l2.get(key);
        if (l2Value != null) {
            return Mono.delay(Duration.ofMillis(L2_SIMULATED_DELAY_MS))
                    .map(d -> {
                        l1.put(key, l2Value);
                        return l2Value;
                    });
        }
        return source.doOnNext(v -> {
            l1.put(key, v);
            l2.put(key, v);
        });
    }

    public void invalidate(String key) {
        l1.invalidate(key);
        l2.remove(key);
    }
}

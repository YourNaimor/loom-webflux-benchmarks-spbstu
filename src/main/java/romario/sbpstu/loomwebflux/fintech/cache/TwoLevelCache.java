package romario.sbpstu.loomwebflux.fintech.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
public class TwoLevelCache {

    private static final Duration L1_TTL = Duration.ofMinutes(5);
    private static final long L2_SIMULATED_DELAY_MS = 15;

    private final Cache<String, Object> l1 = Caffeine.newBuilder()
            .expireAfterWrite(L1_TTL)
            .maximumSize(10_000)
            .build();

    private final Map<String, Object> l2 = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Supplier<T> source) {
        T value = (T) l1.getIfPresent(key);
        if (value != null) {
            return value;
        }
        value = (T) l2.get(key);
        if (value != null) {
            try { Thread.sleep(L2_SIMULATED_DELAY_MS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            l1.put(key, value);
            return value;
        }
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

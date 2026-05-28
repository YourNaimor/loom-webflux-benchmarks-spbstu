package romario.sbpstu.loomwebflux.fintech.balancer;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.TreeMap;

@Component
public class HashRingLoadBalancer {

    private static final int VIRTUAL_NODES = 150;

    private final TreeMap<Long, String> ring = new TreeMap<>();

    public HashRingLoadBalancer() {
        List<String> nodes = List.of("/mock", "/mock", "/mock");
        for (String node : nodes) {
            for (int i = 0; i < VIRTUAL_NODES; i++) {
                ring.put(hash(node + "-vn-" + i), node);
            }
        }
    }

    public String selectNode(String key) {
        if (ring.isEmpty()) {
            throw new IllegalStateException("Hash ring is empty");
        }
        long h = hash(key);
        var entry = ring.ceilingEntry(h);
        if (entry == null) {
            entry = ring.firstEntry();
        }
        return entry.getValue();
    }

    private long hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(key.getBytes(StandardCharsets.UTF_8));
            long h = 0;
            for (int i = 0; i < 4; i++) {
                h <<= 8;
                h |= (digest[i] & 0xFF);
            }
            return h;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

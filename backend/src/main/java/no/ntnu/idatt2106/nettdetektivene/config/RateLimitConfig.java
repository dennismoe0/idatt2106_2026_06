package no.ntnu.idatt2106.nettdetektivene.config;

import io.github.bucket4j.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configures token-bucket rate limiting for authentication endpoints using
 * Bucket4j. A shared per-IP bucket map is provided as a bean; each IP starts
 * with an allowance of 10 requests per minute (greedy refill).
 */
@Configuration
public class RateLimitConfig {

    /**
     * In-memory map from IP address (or other key) to its rate-limit bucket.
     * Callers obtain or create a bucket on first access.
     */
    @Bean
    public Map<String, Bucket> authRateLimitBuckets() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Creates a new rate-limit bucket configured to allow 10 requests per minute
     * with greedy refill. Call this when a new IP is seen for the first time.
     *
     * @return a freshly initialised {@link Bucket}
     */
    public static Bucket newBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.builder()
                .capacity(10)
                .refillGreedy(10, Duration.ofMinutes(1))
                .build())
            .build();
    }
}

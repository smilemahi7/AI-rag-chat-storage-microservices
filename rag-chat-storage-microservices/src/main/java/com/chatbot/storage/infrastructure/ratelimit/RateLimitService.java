package com.chatbot.storage.infrastructure.ratelimit;

import com.chatbot.storage.config.properties.RateLimitingProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 *
 * Redis-based Rate limit service.
 */
@Service
@Slf4j
public class RateLimitService {

    private final RateLimitingProperties properties;
    private final RedisClient redisClient;
    private StatefulRedisConnection<String, byte[]> redisConnection;
    private LettuceBasedProxyManager<String> proxyManager;

    public RateLimitService(
            RateLimitingProperties properties,
            @Value("${spring.redis.host}") String redisHost,
            @Value("${spring.redis.port}") int redisPort,
            @Value("${spring.redis.password:}") String redisPassword) {

        this.properties = properties;

        // Build Redis URI
        RedisURI.Builder uriBuilder = RedisURI.Builder.redis(redisHost, redisPort);
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            uriBuilder.withPassword(redisPassword.toCharArray());
        }

        this.redisClient = RedisClient.create(uriBuilder.build());
    }

    @PostConstruct
    public void init() {
        // Create connection with proper codec for String keys and byte[] values
        this.redisConnection = redisClient.connect(
                RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
        );

        // Use the correct API for version 8.15.0
        this.proxyManager = Bucket4jLettuce.casBasedBuilder(redisConnection)
                .build();

        log.info("Redis-based rate limiting initialized with {}req/{}h",
                properties.getRequests(), properties.getHours());
    }

    /**
     * Try consume boolean.
     *
     * @param key the key
     * @return the boolean
     */
    public boolean tryConsume(String key) {
        // Skip rate limiting if disabled
        if (!properties.isEnabled()) {
            return true;
        }

        try {
            Bucket bucket = proxyManager.builder().build(key, this::createBucketConfiguration);
            boolean consumed = bucket.tryConsume(1);

            if (!consumed) {
                log.warn("Rate limit exceeded for key: {}", key);
            }

            return consumed;
        } catch (Exception e) {
            log.error("Error checking rate limit for key: {}", key, e);
            // Fail open - allow request if Redis is down
            return true;
        }
    }

    private BucketConfiguration createBucketConfiguration() {
        // Use configuration values instead of constants
        Bandwidth limit = Bandwidth.classic(
                properties.getRequests(),
                Refill.intervally(properties.getRequests(), Duration.ofHours(properties.getHours()))
        );

        return BucketConfiguration.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Clear bucket.
     *
     * @param key the key
     */
    public void clearBucket(String key) {
        try {
            proxyManager.removeProxy(key);
            log.info("Cleared rate limit bucket for key: {}", key);
        } catch (Exception e) {
            log.error("Failed to clear bucket for key: {}", key, e);
        }
    }

    /**
     * Clear all buckets (be careful with this in production).
     */
    public void clearAllBuckets() {
        try {
            // This is a simple approach - you might want to implement a more sophisticated cleanup
            log.warn("clearAllBuckets called - this operation is not directly supported by Bucket4j Redis");
        } catch (Exception e) {
            log.error("Failed to clear all buckets", e);
        }
    }

    /**
     * Get current bucket count.
     * Note: This is not efficiently supported in Redis-based implementation
     *
     * @return -1 to indicate unsupported operation
     */
    public int getBucketCount() {
        log.debug("getBucketCount called - not efficiently supported in Redis implementation");
        return -1; // Not efficiently supported in Redis implementation
    }

    @PreDestroy
    public void cleanup() {
        if (redisConnection != null) {
            redisConnection.close();
        }
        if (redisClient != null) {
            redisClient.shutdown();
        }
        log.info("Redis rate limiting resources cleaned up");
    }
}
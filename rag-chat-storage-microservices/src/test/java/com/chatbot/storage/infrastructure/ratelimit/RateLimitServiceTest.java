package com.chatbot.storage.infrastructure.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * The type Rate limit service test.
 */
@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @InjectMocks
    private RateLimitService rateLimitService;

    private String testKey;
    private String anotherKey;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        testKey = "test-api-key-123";
        anotherKey = "another-api-key-456";
    }

    /**
     * Try to consume first request should return true.
     */
    @Test
    void tryConsume_FirstRequest_ShouldReturnTrue() {
        // When
        boolean result = rateLimitService.tryConsume(testKey);

        // Then
        assertTrue(result);
    }

    /**
     * Try to consume multiple requests within limit should return true.
     */
    @Test
    void tryConsume_MultipleRequestsWithinLimit_ShouldReturnTrue() {
        // When & Then
        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimitService.tryConsume(testKey), 
                    "Request " + (i + 1) + " should be allowed");
        }
    }

    /**
     * Try to consume different keys should have independent buckets.
     */
    @Test
    void tryConsume_DifferentKeys_ShouldHaveIndependentBuckets() {
        // When & Then
        // Consume requests for first key
        for (int i = 0; i < 50; i++) {
            assertTrue(rateLimitService.tryConsume(testKey));
        }

        // Second key should still work independently
        for (int i = 0; i < 50; i++) {
            assertTrue(rateLimitService.tryConsume(anotherKey));
        }
    }

    /**
     * Try to consume same key repeated should create bucket once.
     *
     * @throws Exception the exception
     */
    @Test
    void tryConsume_SameKeyRepeated_ShouldCreateBucketOnce() throws Exception {
        // Given - access the private buckets field to verify bucket creation
        Field bucketsField = RateLimitService.class.getDeclaredField("buckets");
        bucketsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, ?> buckets = (Map<String, ?>) bucketsField.get(rateLimitService);

        // When
        rateLimitService.tryConsume(testKey);
        int bucketsAfterFirst = buckets.size();
        
        rateLimitService.tryConsume(testKey);
        int bucketsAfterSecond = buckets.size();

        // Then
        assertEquals(1, bucketsAfterFirst);
        assertEquals(1, bucketsAfterSecond);
        assertTrue(buckets.containsKey(testKey));
    }

    /**
     * Try to consume excessive requests should return false when limit exceeded.
     */
    @Test
    void tryConsume_ExcessiveRequests_ShouldReturnFalseWhenLimitExceeded() {
        // Given - consume up to the limit (100 requests per hour by default)
        for (int i = 0; i < 100; i++) {
            assertTrue(rateLimitService.tryConsume(testKey), 
                    "Request " + (i + 1) + " should be within limit");
        }

        // When - try one more request beyond the limit
        boolean result = rateLimitService.tryConsume(testKey);

        // Then
        assertFalse(result, "Request beyond limit should be rejected");
    }

    /**
     * Try to consume after exceeding limit should continue returning false.
     */
    @Test
    void tryConsume_AfterExceedingLimit_ShouldContinueReturningFalse() {
        // Given - exceed the rate limit
        for (int i = 0; i < 101; i++) {
            rateLimitService.tryConsume(testKey);
        }

        // When & Then - multiple requests after limit should all fail
        for (int i = 0; i < 5; i++) {
            assertFalse(rateLimitService.tryConsume(testKey), 
                    "Request after exceeding limit should be rejected");
        }
    }

    /**
     * Clear bucket should remove bucket from cache.
     *
     * @throws Exception the exception
     */
    @Test
    void clearBucket_ShouldRemoveBucketFromCache() throws Exception {
        // Given
        rateLimitService.tryConsume(testKey); // Create bucket
        
        Field bucketsField = RateLimitService.class.getDeclaredField("buckets");
        bucketsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, ?> buckets = (Map<String, ?>) bucketsField.get(rateLimitService);

        assertTrue(buckets.containsKey(testKey));

        // When
        rateLimitService.clearBucket(testKey);

        // Then
        assertFalse(buckets.containsKey(testKey));
        assertEquals(0, buckets.size());
    }

    /**
     * Clear bucket non-existent key should not throw exception.
     */
    @Test
    void clearBucket_NonExistentKey_ShouldNotThrowException() {
        // When & Then - should not throw any exception
        assertDoesNotThrow(() -> rateLimitService.clearBucket("non-existent-key"));
    }

    /**
     * Clear bucket after clearing should allow new requests.
     */
    @Test
    void clearBucket_AfterClearing_ShouldAllowNewRequests() {
        // Given - consume all requests and exceed limit
        for (int i = 0; i < 101; i++) {
            rateLimitService.tryConsume(testKey);
        }
        
        // Verify limit is exceeded
        assertFalse(rateLimitService.tryConsume(testKey));

        // When - clear the bucket
        rateLimitService.clearBucket(testKey);

        // Then - should allow new requests (creates new bucket)
        assertTrue(rateLimitService.tryConsume(testKey));
    }

    /**
     * Try to consume with empty key should create bucket.
     */
    @Test
    void tryConsume_WithEmptyKey_ShouldCreateBucket() {
        // When
        boolean result = rateLimitService.tryConsume("");

        // Then
        assertTrue(result);
    }

    /**
     * Clear bucket multiple keys should only clear specified key.
     *
     * @throws Exception the exception
     */
    @Test
    void clearBucket_MultipleKeys_ShouldOnlyClearSpecifiedKey() throws Exception {
        // Given
        rateLimitService.tryConsume(testKey);
        rateLimitService.tryConsume(anotherKey);
        
        Field bucketsField = RateLimitService.class.getDeclaredField("buckets");
        bucketsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, ?> buckets = (Map<String, ?>) bucketsField.get(rateLimitService);

        assertEquals(2, buckets.size());

        // When
        rateLimitService.clearBucket(testKey);

        // Then
        assertEquals(1, buckets.size());
        assertFalse(buckets.containsKey(testKey));
        assertTrue(buckets.containsKey(anotherKey));
    }
}
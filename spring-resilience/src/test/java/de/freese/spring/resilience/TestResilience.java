// Created: 09.04.2021
package de.freese.spring.resilience;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.Retry.Metrics;
import io.github.resilience4j.retry.RetryConfig;

/**
 * @author Thomas Freese
 */
class TestResilience
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TestResilience.class);

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testDecorators() throws Exception
    {
        Object data = new Object();

        Callable<Object> failingCode = () -> {
            double value = Math.random();

            if (value < 0.5D)
            {
                LOGGER.info("throw Exception: {}", value);
                throw new Exception("test exception");
            }

            LOGGER.info("return data: {}", value);
            return data;
        };

        // CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("backendService");
        CircuitBreaker circuitBreaker = CircuitBreaker.of("backendService", CircuitBreakerConfig.custom().failureRateThreshold(50F).build());
        circuitBreaker.getEventPublisher().onError(event -> LOGGER.error(event.toString()));

        // Retry retry = Retry.ofDefaults("backendService");
        Retry retry = Retry.of("backendService", RetryConfig.custom().maxAttempts(10).waitDuration(Duration.ofMillis(100)).build());
        retry.getEventPublisher().onRetry(event -> LOGGER.info(event.toString()));
        retry.getEventPublisher().onSuccess(event -> LOGGER.info(event.toString()));

        // Bulkhead bulkhead = Bulkhead.ofDefaults("backendService");
        Bulkhead bulkhead = Bulkhead.of("backendService", BulkheadConfig.custom().maxConcurrentCalls(5).maxWaitDuration(Duration.ofMillis(10)).build());
        bulkhead.getEventPublisher().onCallRejected(event -> LOGGER.error(event.toString()));

        // @formatter:off
        Callable<Object> decoratedSupplier = Decorators.ofCallable(failingCode)
                .withCircuitBreaker(circuitBreaker)//.withFallback(th -> data)
                .withRetry(retry)
                .withBulkhead(bulkhead)
                .decorate()
                ;
        // @formatter:on

        AtomicReference<Object> valueReference = new AtomicReference<>();

        IntStream.range(0, 10).parallel().forEach(i -> {
            try
            {
                Object value = decoratedSupplier.call();

                if (value != null)
                {
                    valueReference.set(value);
                }
            }
            catch (Exception ex)
            {
                // Ignore
            }
        });

        assertEquals(data, valueReference.get());
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testRateLimiter() throws Exception
    {
        // 10 Requests/Second
        RateLimiterConfig config = RateLimiterConfig.custom().limitForPeriod(10).limitRefreshPeriod(Duration.ofSeconds(1)).build();
        // .timeoutDuration(Duration.ofSeconds(1))

        RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(config);

        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("name1");

        int index = 0;

        while (index < 100)
        {
            int[] buffer = new int[10];

            for (int i = 0; i < 10; i++)
            {
                rateLimiter.acquirePermission(1);
                buffer[i] = index;
                index++;
            }

            LOGGER.info("RateLimiter: {}", Arrays.toString(buffer));
        }
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testRetry() throws Exception
    {
        Object data = new Object();

        Callable<Object> failingCode = () -> {
            double value = Math.random();

            if (value < 0.95D)
            {
                LOGGER.info("throw Exception: {}", value);
                throw new Exception("test exception");
            }

            LOGGER.info("return data: {}", value);
            return data;
        };

        RetryConfig retryConfig = RetryConfig.custom().maxAttempts(10).waitDuration(Duration.ofMillis(100)).build();
        Retry retry = Retry.of("test-retry", retryConfig);

        retry.getEventPublisher().onRetry(event -> LOGGER.info("onRetry: try={}", event.getNumberOfRetryAttempts()));
        retry.getEventPublisher().onSuccess(event -> LOGGER.info("onSuccess: tries={}", event.getNumberOfRetryAttempts()));

        // Callable<Object> retryableCallable = Retry.decorateCallable(retry, failingCode);
        // Object value = Try.ofCallable(retryableCallable).get();

        try
        {
            Object value = retry.executeCallable(failingCode);
            assertEquals(data, value);
        }
        catch (Exception ex)
        {
            assertEquals("test exception", ex.getMessage());
        }

        Metrics metrics = retry.getMetrics();
        LOGGER.info("{}, {}, {}, {}", metrics.getNumberOfFailedCallsWithoutRetryAttempt(), metrics.getNumberOfFailedCallsWithRetryAttempt(),
                metrics.getNumberOfSuccessfulCallsWithoutRetryAttempt(), metrics.getNumberOfSuccessfulCallsWithRetryAttempt());
    }
}

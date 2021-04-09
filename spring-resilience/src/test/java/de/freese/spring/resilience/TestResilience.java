// Created: 09.04.2021
package de.freese.spring.resilience;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

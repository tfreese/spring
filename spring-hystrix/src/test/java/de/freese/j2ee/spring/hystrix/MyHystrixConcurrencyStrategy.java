/**
 * Created: 17.12.2016
 */

package de.freese.j2ee.spring.hystrix;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.properties.HystrixProperty;

/**
 * Was eigentlich bei Hystrix NICHT sein soll, nur einen ThreadPool für alles !
 *
 * @author Thomas Freese
 */
public class MyHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy
{
    /**
     *
     */
    private final ThreadPoolExecutor executor;

    /**
     * Erstellt ein neues {@link MyHystrixConcurrencyStrategy} Object.
     *
     * @param executor {@link ThreadPoolExecutor}
     */
    public MyHystrixConcurrencyStrategy(final ThreadPoolExecutor executor)
    {
        super();

        this.executor = Objects.requireNonNull(executor, "executor required");
    }

    /**
     * @see com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy#getThreadPool(com.netflix.hystrix.HystrixThreadPoolKey,
     *      com.netflix.hystrix.strategy.properties.HystrixProperty, com.netflix.hystrix.strategy.properties.HystrixProperty,
     *      com.netflix.hystrix.strategy.properties.HystrixProperty, java.util.concurrent.TimeUnit, java.util.concurrent.BlockingQueue)
     */
    @Override
    public ThreadPoolExecutor getThreadPool(final HystrixThreadPoolKey threadPoolKey, final HystrixProperty<Integer> corePoolSize,
            final HystrixProperty<Integer> maximumPoolSize, final HystrixProperty<Integer> keepAliveTime, final TimeUnit unit,
            final BlockingQueue<Runnable> workQueue)
    {
        // return super.getThreadPool(threadPoolKey, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        return this.executor;
    }

    /**
     * @see com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy#getThreadPool(com.netflix.hystrix.HystrixThreadPoolKey,
     *      com.netflix.hystrix.HystrixThreadPoolProperties)
     */
    @Override
    public ThreadPoolExecutor getThreadPool(final HystrixThreadPoolKey threadPoolKey,
            final HystrixThreadPoolProperties threadPoolProperties)
    {
        // return super.getThreadPool(threadPoolKey, threadPoolProperties);
        return this.executor;
    }
}

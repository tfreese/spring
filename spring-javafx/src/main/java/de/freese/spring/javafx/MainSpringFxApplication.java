/**
 * Created: 09.02.2019
 */
package de.freese.spring.javafx;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import javafx.application.Application;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
public class MainSpringFxApplication
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        Application.launch(JavaFxApplication.class, args);
    }

    /**
     * Irrelevant, nur zum Testen.
     *
     * @return {@link ThreadPoolExecutorFactoryBean}
     */
    @Bean
    @ConditionalOnMissingBean(ExecutorService.class)
    @Primary
    public ThreadPoolExecutorFactoryBean executorService()
    {
        int coreSize = Runtime.getRuntime().availableProcessors() * 2;
        int maxSize = coreSize * 2;
        int queueSize = maxSize * 2;
        int keepAliveSeconds = 60;

        ThreadPoolExecutorFactoryBean bean = new ThreadPoolExecutorFactoryBean();
        bean.setCorePoolSize(coreSize);
        bean.setMaxPoolSize(maxSize);
        bean.setQueueCapacity(queueSize);
        bean.setKeepAliveSeconds(keepAliveSeconds);
        bean.setThreadPriority(Thread.NORM_PRIORITY);
        bean.setThreadNamePrefix("pool-");
        bean.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        bean.setAllowCoreThreadTimeOut(false);
        bean.setExposeUnconfigurableExecutor(true);

        return bean;
    }

    // /**
    // * @return {@link ScheduledExecutorFactoryBean}
    // */
    // @Bean
    // @ConditionalOnMissingBean(ScheduledExecutorService.class)
    // public ScheduledExecutorFactoryBean scheduledExecutorService()
    // {
    // int poolSize = Runtime.getRuntime().availableProcessors();
    //
    // ScheduledExecutorFactoryBean bean = new ScheduledExecutorFactoryBean();
    // bean.setPoolSize(poolSize);
    // bean.setThreadPriority(Thread.NORM_PRIORITY);
    // bean.setThreadNamePrefix("scheduler-");
    // bean.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    // bean.setExposeUnconfigurableExecutor(true);
    //
    // return bean;
    // }
    //
    // /**
    // * Wird für {@link EnableAsync} benötigt.
    // *
    // * @return {@link TaskExecutor}
    // */
    // @Bean(
    // {
    // "taskExecutor", "asyncTaskExecutor"
    // })
    // @ConditionalOnMissingBean(AsyncTaskExecutor.class)
    // // public TaskExecutor springTaskExecutor(@Qualifier("executorService") final ExecutorService executorService)
    // public AsyncTaskExecutor springTaskExecutor()
    // {
    // AsyncTaskExecutor bean = new ConcurrentTaskExecutor(executorService);
    //
    // return bean;
    // }
    //
    // /**
    // * Wird für {@link EnableScheduling} benötigt.
    // *
    // * @param executorService {@link ExecutorService}
    // * @param scheduledExecutorService {@link ScheduledExecutorService}
    // * @return {@link TaskScheduler}
    // */
    // @Bean("taskScheduler")
    // @ConditionalOnMissingBean(TaskScheduler.class)
    // public TaskScheduler springTaskScheduler(@Qualifier("executorService") final ExecutorService executorService,
    // final ScheduledExecutorService scheduledExecutorService)
    // {
    // TaskScheduler bean = new ConcurrentTaskScheduler(executorService, scheduledExecutorService);
    //
    // return bean;
    // }
}

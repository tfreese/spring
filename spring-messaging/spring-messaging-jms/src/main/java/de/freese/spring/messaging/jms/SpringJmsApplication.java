// Created: 31.01.2019
package de.freese.spring.messaging.jms;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import javax.jms.ConnectionFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
@EnableJms
public class SpringJmsApplication
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        SpringApplication.run(SpringJmsApplication.class, args);
    }

    /**
     * @return {@link ThreadPoolExecutorFactoryBean}
     */
    @Bean
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

    /**
     * Serialize message content to json using TextMessage
     *
     * @return {@link MessageConverter}
     */
    @Bean
    public MessageConverter jacksonJmsMessageConverter()
    {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        return converter;
    }

    /**
     * @param connectionFactory {@link ConnectionFactory}
     * @param configurer {@link DefaultJmsListenerContainerFactoryConfigurer}
     * @param taskExecutor {@link Executor}
     *
     * @return {@link JmsListenerContainerFactory}
     */
    @Bean
    public JmsListenerContainerFactory<?> myFactory(final ConnectionFactory connectionFactory, final DefaultJmsListenerContainerFactoryConfigurer configurer,
                                                    final Executor taskExecutor)
    {
        // This provides all boot's default to this factory, including the message converter
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setTaskExecutor(taskExecutor);

        // You could still override some of Boot's default if necessary.
        configurer.configure(factory, connectionFactory);

        return factory;
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

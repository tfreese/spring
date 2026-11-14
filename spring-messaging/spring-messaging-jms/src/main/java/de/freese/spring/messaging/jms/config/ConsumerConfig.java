package de.freese.spring.messaging.jms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.freese.spring.messaging.jms.JmsConsumer;

/**
 * @author Thomas Freese
 * @since 22.10.2022
 */
@Configuration
public class ConsumerConfig {
    // /**
    //  * Cache for sessions / producers / consumers. This is the usual JMS-level cache and does not create a second
    //  * {@link ConnectionFactory} bean, so no circular dependency with Boot's auto-config.
    //  */
    // @Bean
    // public CachingConnectionFactory cachingConnectionFactory(final ConnectionFactory connectionFactory) {
    //     final CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(connectionFactory);
    //     cachingConnectionFactory.setSessionCacheSize(10);
    //     cachingConnectionFactory.setCacheConsumers(true);
    //     cachingConnectionFactory.setCacheProducers(true);
    //
    //     return cachingConnectionFactory;
    // }
    //
    // @Bean
    // public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(final ConnectionFactory connectionFactory, final Executor taskExecutor) {
    //     final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    //     factory.setConnectionFactory(connectionFactory);
    //     factory.setSessionTransacted(true);
    //     factory.setConcurrency("2-10");
    //     factory.setTaskExecutor(taskExecutor);
    //
    //     return factory;
    // }
    //
    // @Bean
    // public ActiveMQConnectionFactory senderActiveMQConnectionFactory(final @Value("${artemis.broker-url}") String brokerUrl) {
    //     return new ActiveMQConnectionFactory(brokerUrl);
    // }
    //
    // @Bean
    // public ActiveMQConnectionFactory receiverActiveMQConnectionFactory(final @Value("${artemis.broker-url}") String brokerUrl) {
    //     return new ActiveMQConnectionFactory(brokerUrl);
    // }

    @Bean
    public JmsConsumer consumer() {
        return new JmsConsumer();
    }
}
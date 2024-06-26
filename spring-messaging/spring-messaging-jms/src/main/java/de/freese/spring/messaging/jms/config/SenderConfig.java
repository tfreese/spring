// Created: 22.10.22
package de.freese.spring.messaging.jms.config;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;

import de.freese.spring.messaging.jms.JmsSender;

/**
 * @author Thomas Freese
 */
@Configuration
public class SenderConfig {
    @Bean
    public CachingConnectionFactory cachingConnectionFactory(final ActiveMQConnectionFactory senderActiveMQConnectionFactory) {
        final CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(senderActiveMQConnectionFactory);
        cachingConnectionFactory.setSessionCacheSize(10);

        return cachingConnectionFactory;
    }

    @Bean
    public JmsTemplate jmsTemplate(final CachingConnectionFactory cachingConnectionFactory, final MessageConverter jacksonJmsMessageConverter) {
        final JmsTemplate jmsTemplate = new JmsTemplate(cachingConnectionFactory);
        jmsTemplate.setMessageConverter(jacksonJmsMessageConverter);
        jmsTemplate.setReceiveTimeout(5000);

        return jmsTemplate;
    }

    @Bean
    public JmsSender sender(final JmsTemplate jmsTemplate) {
        return new JmsSender(jmsTemplate);
    }

    @Bean
    public ActiveMQConnectionFactory senderActiveMQConnectionFactory(final @Value("${artemis.broker-url}") String brokerUrl) {
        return new ActiveMQConnectionFactory(brokerUrl);
    }
}

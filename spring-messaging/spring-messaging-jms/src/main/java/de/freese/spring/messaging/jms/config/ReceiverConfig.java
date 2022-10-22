// Created: 22.10.22
package de.freese.spring.messaging.jms.config;

import java.util.concurrent.Executor;

import de.freese.spring.messaging.jms.JmsReceiver;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

/**
 * @author Thomas Freese
 */
@Configuration
public class ReceiverConfig
{
    @Value("${artemis.broker-url}")
    private String brokerUrl;

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ActiveMQConnectionFactory receiverActiveMQConnectionFactory
            , DefaultJmsListenerContainerFactoryConfigurer configurer, Executor taskExecutor)
    {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(receiverActiveMQConnectionFactory);
        //        factory.setMessageConverter(jacksonJmsMessageConverter);
        factory.setTaskExecutor(taskExecutor);
        factory.setConcurrency("2-10");

        // Hier drin erfolgt die Konfiguration des MessageConverters.
        configurer.configure(factory, receiverActiveMQConnectionFactory);

        return factory;
    }

    @Bean
    public JmsReceiver receiver()
    {
        return new JmsReceiver();
    }

    @Bean
    public ActiveMQConnectionFactory receiverActiveMQConnectionFactory()
    {
        return new ActiveMQConnectionFactory(brokerUrl);
    }
}

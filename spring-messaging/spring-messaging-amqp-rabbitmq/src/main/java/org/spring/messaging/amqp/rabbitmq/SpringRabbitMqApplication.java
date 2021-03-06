/**
 * Created: 31.01.2019
 */
package org.spring.messaging.amqp.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
@EnableRabbit
public class SpringRabbitMqApplication
{
    /**
     *
     */
    public static final String QUEUE_NAME = "spring-boot";

    /**
     *
     */
    public static final String TOPIC_EXCHANGE_NAME = "spring-boot-exchange";

    /**
     * @param args String[]
     */
    @SuppressWarnings("resource")
    public static void main(final String[] args)
    {
        SpringApplication.run(SpringRabbitMqApplication.class, args);
    }

    /**
     * @param queue {@link Queue}
     * @param exchange {@link TopicExchange}
     * @return {@link Binding}
     */
    @Bean
    public Binding binding(final Queue queue, final TopicExchange exchange)
    {
        return BindingBuilder.bind(queue).to(exchange).with("foo.bar.#");
    }

    /**
     * @return {@link TopicExchange}
     */
    @Bean
    public TopicExchange exchange()
    {
        return new TopicExchange(TOPIC_EXCHANGE_NAME);
    }

    /**
     * @return {@link Jackson2JsonMessageConverter}
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2MessageConverter()
    {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * @return {@link Queue}
     */
    @Bean
    public Queue queue()
    {
        return new Queue(QUEUE_NAME, false);
    }

    // /**
    // * @param receiver {@link Receiver}
    // * @return {@link MessageListenerAdapter}
    // */
    // @Bean
    // public MessageListenerAdapter myMessageListenerAdapter(final Receiver receiver)
    // {
    // return new MessageListenerAdapter(receiver, "receiveMessage");
    // }

    // /**
    // * @param connectionFactory {@link ConnectionFactory}
    // * @param listenerAdapter {@link MessageListenerAdapter}
    // * @return {@link MessageListenerContainer}
    // */
    // @Bean
    // public MessageListenerContainer myMmessageListenerContainer(final ConnectionFactory connectionFactory, final MessageListenerAdapter
    // myMessageListenerAdapter)
    // {
    // SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    // container.setConnectionFactory(connectionFactory);
    // container.setQueueNames(QUEUE_NAME);
    // container.setMessageListener(myMessageListenerAdapter);
    //
    // return container;
    // }
}

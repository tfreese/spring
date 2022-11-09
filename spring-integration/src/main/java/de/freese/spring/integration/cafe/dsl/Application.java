// Created: 01.02.2018
package de.freese.spring.integration.cafe.dsl;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import de.freese.spring.integration.cafe.Delivery;
import de.freese.spring.integration.cafe.Drink;
import de.freese.spring.integration.cafe.Order;
import de.freese.spring.integration.cafe.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;

/**
 * https://github.com/spring-projects/spring-integration-samples/blob/master/dsl/cafe-dsl/src/main/java/org/springframework/integration/samples/dsl/cafe/lambda/Application.java
 *
 * @author Thomas Freese
 */
@SpringBootApplication
// @EnableIntegration
public class Application
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    /**
     * @author Thomas Freese
     */
    @MessagingGateway
    public interface Cafe extends de.freese.spring.integration.cafe.Cafe
    {
        /**
         * @see de.freese.spring.integration.cafe.Cafe#placeOrder(de.freese.spring.integration.cafe.Order)
         */
        @Override
        @Gateway(requestChannel = "orders.input")
        void placeOrder(Order order);
    }

    private static void sleep(final long millis)
    {
        try
        {
            TimeUnit.MILLISECONDS.sleep(millis);
        }
        catch (Exception ex)
        {
            // Empty
        }
    }

    private final AtomicInteger coldDrinkCounter = new AtomicInteger();

    private final AtomicInteger hotDrinkCounter = new AtomicInteger();

    @Bean
    public IntegrationFlow orders()
    {
        //@formatter:off
        return f -> f
                .split(Order.class,
                        Order::getItems)
                .channel(
                        c -> c.executor(
                                Executors.newCachedThreadPool()))
                .<OrderItem, Boolean> route(OrderItem::isIced,
                        mapping -> mapping
                                .subFlowMapping(true,
                                        sf -> sf.channel(c -> c.queue(10))
                                                .publishSubscribeChannel(c -> c.subscribe(s -> s.handle(m -> sleep(400)))
                                                        .subscribe(sub -> sub
                                                                .<OrderItem, String> transform(p -> Thread.currentThread().getName()
                                                                        + " prepared cold drink #" + this.coldDrinkCounter.incrementAndGet()
                                                                        + " for order #" + p.getOrderNumber() + ": " + p)
                                                                .handle(m -> LOGGER.info("{}", m.getPayload())))))
                                .subFlowMapping(false,
                                        sf -> sf.channel(c -> c.queue(10))
                                                .publishSubscribeChannel(c -> c.subscribe(s -> s.handle(m -> sleep(800)))
                                                        .subscribe(sub -> sub
                                                                .<OrderItem, String> transform(p -> Thread.currentThread().getName()
                                                                        + " prepared hot drink #" + this.hotDrinkCounter.incrementAndGet()
                                                                        + " for order #" + p.getOrderNumber() + ": " + p)
                                                                .handle(m -> LOGGER.info("{}", m.getPayload())))))
                                .defaultOutputToParentFlow())
                .<OrderItem, Drink> transform(
                        orderItem -> new Drink(orderItem.getOrderNumber(), orderItem.getDrinkType(), orderItem.isIced()))
                .aggregate(aggregator -> aggregator
                        .outputProcessor(g -> new Delivery(
                                g.getMessages().stream().map(message -> (Drink) message.getPayload()).toList()))
                        .correlationStrategy(m -> ((Drink) m.getPayload()).getOrderNumber()))
//                .handle(CharacterStreamWritingMessageHandler.stdout());
//         .handle((MessageHandler)obj -> {
//             System.out.println("Result: " + obj.toString());
//             })
         ;
        //@formatter:on
    }

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata poller()
    {
        return Pollers.fixedDelay(500).maxMessagesPerPoll(1).get();
    }
}

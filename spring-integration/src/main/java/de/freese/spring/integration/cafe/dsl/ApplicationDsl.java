// Created: 01.02.2018
package de.freese.spring.integration.cafe.dsl;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;

import de.freese.spring.integration.cafe.Cafe;
import de.freese.spring.integration.cafe.Delivery;
import de.freese.spring.integration.cafe.Drink;
import de.freese.spring.integration.cafe.Order;
import de.freese.spring.integration.cafe.OrderItem;

/**
 * <a href="
 * https://github.com/spring-projects/spring-integration-samples/blob/master/dsl/cafe-dsl/src/main/java/org/springframework/integration/samples/dsl/cafe/lambda/Application.java
 * ">spring-integration-samples</a>
 *
 * @author Thomas Freese
 */
@SpringBootApplication
// @EnableIntegration
public class ApplicationDsl {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationDsl.class);

    /**
     * @author Thomas Freese
     */
    @MessagingGateway
    public interface CafeDsl extends Cafe {
        @Override
        @Gateway(requestChannel = "orders.input")
        void placeOrder(Order order);
    }

    private static void sleep(final long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        }
        catch (InterruptedException ex) {
            // Restore interrupted state.
            Thread.currentThread().interrupt();
        }
        catch (Exception ex) {
            // Empty
        }
    }

    private final AtomicInteger coldDrinkCounter = new AtomicInteger();
    private final AtomicInteger hotDrinkCounter = new AtomicInteger();

    @Bean
    public IntegrationFlow orders() {
        return f -> f
                .split(Order.class, Order::getItems)
                .channel(c -> c.executor(Executors.newCachedThreadPool()))
                .<OrderItem, Boolean>route(OrderItem::isIced,
                        mapping -> mapping
                                .subFlowMapping(true,
                                        sf -> sf.channel(c -> c.queue(10))
                                                .publishSubscribeChannel(c -> c.subscribe(s -> s.handle(m -> sleep(400L)))
                                                        .subscribe(sub -> sub
                                                                .<OrderItem, String>transform(p -> Thread.currentThread().getName()
                                                                        + " prepared cold drink #" + coldDrinkCounter.incrementAndGet()
                                                                        + " for order #" + p.getOrderNumber() + ": " + p)
                                                                .handle(m -> LOGGER.info("{}", m.getPayload())))))
                                .subFlowMapping(false,
                                        sf -> sf.channel(c -> c.queue(10))
                                                .publishSubscribeChannel(c -> c.subscribe(s -> s.handle(m -> sleep(800L)))
                                                        .subscribe(sub -> sub
                                                                .<OrderItem, String>transform(p -> Thread.currentThread().getName()
                                                                        + " prepared hot drink #" + hotDrinkCounter.incrementAndGet()
                                                                        + " for order #" + p.getOrderNumber() + ": " + p)
                                                                .handle(m -> LOGGER.info("{}", m.getPayload())))))
                                .defaultOutputToParentFlow())
                .<OrderItem, Drink>transform(
                        orderItem -> new Drink(orderItem.getOrderNumber(), orderItem.getDrinkType(), orderItem.isIced()))
                .aggregate(aggregator -> aggregator
                        .outputProcessor(g ->
                                new Delivery(g.getMessages().stream().map(message -> (Drink) message.getPayload()).toList()))
                        .correlationStrategy(m -> ((Drink) m.getPayload()).getOrderNumber()))
                //                .handle(CharacterStreamWritingMessageHandler.stdout());
                //         .handle((MessageHandler)obj -> {
                //             System.out.println("Result: " + obj.toString());
                //             })
                ;
    }

    @Bean(PollerMetadata.DEFAULT_POLLER)
    @SuppressWarnings("java:S6830")
    public PollerMetadata poller() {
        return Pollers.fixedDelay(500L).maxMessagesPerPoll(1L).getObject();
    }
}

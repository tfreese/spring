// Created: 01.02.2018
package de.freese.spring.integration.cafe.config;

import java.util.List;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.Aggregator;
import org.springframework.integration.annotation.CorrelationStrategy;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.Router;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Splitter;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageChannel;

import de.freese.spring.integration.cafe.Delivery;
import de.freese.spring.integration.cafe.DeliveryLogger;
import de.freese.spring.integration.cafe.Drink;
import de.freese.spring.integration.cafe.Order;
import de.freese.spring.integration.cafe.OrderItem;
import de.freese.spring.integration.cafe.xml.XmlBarista;
import de.freese.spring.integration.cafe.xml.XmlWaiter;

/**
 * https://github.com/spring-projects/spring-integration-samples/blob/master/dsl/cafe-dsl/src/main/java/org/springframework/integration/samples/dsl/cafe/lambda/Application.java
 *
 * @author Thomas Freese
 */
@SpringBootApplication
// @EnableIntegration
public class ApplicationConfig {
    /**
     * @author Thomas Freese
     */
    @MessagingGateway
    public interface Cafe extends de.freese.spring.integration.cafe.Cafe {
        @Override
        @Gateway(requestChannel = "channelOrders")
        void placeOrder(Order order);
    }

    @Aggregator(inputChannel = "channelPreparedDrinks", outputChannel = "channelDeliveries")
    public Delivery aggregator(final List<Drink> drinks) {
        return waiter().prepareDelivery(drinks);
    }

    @CorrelationStrategy
    public int aggregatorCorrelationStrategy(final Drink drink) {
        return drink.getOrderNumber();
    }

    @Bean
    public XmlBarista barista() {
        return new XmlBarista();
    }

    @Bean
    public MessageChannel channelColdDrinks() {
        return new QueueChannel(2);
    }

    @Bean
    public MessageChannel channelHotDrinks() {
        return new QueueChannel(2);
    }

    @ServiceActivator(inputChannel = "channelDeliveries")
    public void delivery(final Delivery delivery) {
        deliveryLogger().log(delivery);
    }

    @Bean
    public DeliveryLogger deliveryLogger() {
        return new DeliveryLogger();
    }

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata poller() {
        return Pollers.fixedDelay(500L).maxMessagesPerPoll(1L).getObject();
    }

    @Router(inputChannel = "channelDrinks")
    public String router(final OrderItem orderItem) {
        return orderItem.isIced() ? "channelColdDrinks" : "channelHotDrinks";
        // RecipientListRouter router = new RecipientListRouter();
        // router.addRecipient("channelColdDrinks", "payload.iced");
        // router.addRecipient("channelHotDrinks", "!payload.iced");
        //
        // return router;
    }

    @ServiceActivator(inputChannel = "channelColdDrinks", outputChannel = "channelPreparedDrinks")
    public Drink serviceActivatorColdDrinks(final OrderItem orderItem) {
        return barista().prepareColdDrink(orderItem);
    }

    @ServiceActivator(inputChannel = "channelHotDrinks", outputChannel = "channelPreparedDrinks")
    public Drink serviceActivatorHotDrinks(final OrderItem orderItem) {
        return barista().prepareHotDrink(orderItem);
    }

    @Splitter(inputChannel = "channelOrders", outputChannel = "channelDrinks")
    public List<OrderItem> splitterOrders(final Order order) {
        return order.getItems();
    }

    @Bean
    public XmlWaiter waiter() {
        return new XmlWaiter();
    }
}

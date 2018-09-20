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
import de.freese.spring.integration.cafe.xml.Barista;
import de.freese.spring.integration.cafe.xml.Waiter;

/**
 * https://github.com/spring-projects/spring-integration-samples/blob/master/dsl/cafe-dsl/src/main/java/org/springframework/integration/samples/dsl/cafe/lambda/Application.java
 *
 * @author Thomas Freese
 */
@SpringBootApplication
// @EnableIntegration
public class Application
{
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
        @Gateway(requestChannel = "channelOrders")
        public void placeOrder(Order order);
    }

    /**
     * Erzeugt eine neue Instanz von {@link Application}.
     */
    public Application()
    {
        super();
    }

    /**
     * @param drinks {@link List}
     * @return {@link Delivery}
     */
    @Aggregator(inputChannel = "channelPreparedDrinks", outputChannel = "channelDeliveries")
    public Delivery aggregator(final List<Drink> drinks)
    {
        return waiter().prepareDelivery(drinks);
    }

    /**
     * @param drink {@link Drink}
     * @return int
     */
    @CorrelationStrategy
    public int aggregatorCorrelationStrategy(final Drink drink)
    {
        return drink.getOrderNumber();
    }

    /**
     * @return {@link Barista}
     */
    @Bean
    public Barista barista()
    {
        return new Barista();
    }

    /**
     * @return {@link MessageChannel}
     */
    @Bean
    public MessageChannel channelColdDrinks()
    {
        return new QueueChannel(2);
    }

    /**
     * @return {@link MessageChannel}
     */
    @Bean
    public MessageChannel channelHotDrinks()
    {
        return new QueueChannel(2);
    }

    /**
     * @param delivery {@link Delivery}
     */
    @ServiceActivator(inputChannel = "channelDeliveries")
    public void delivery(final Delivery delivery)
    {
        deliveryLogger().log(delivery);
    }

    /**
     * @return {@link DeliveryLogger}
     */
    @Bean
    public DeliveryLogger deliveryLogger()
    {
        return new DeliveryLogger();
    }

    /**
     * @return {@link PollerMetadata}
     */
    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata poller()
    {
        return Pollers.fixedDelay(500).maxMessagesPerPoll(1).get();
    }

    /**
     * @param orderItem {@link OrderItem}
     * @return String
     */
    @Router(inputChannel = "channelDrinks")
    public String router(final OrderItem orderItem)
    {
        return orderItem.isIced() ? "channelColdDrinks" : "channelHotDrinks";
        // RecipientListRouter router = new RecipientListRouter();
        // router.addRecipient("channelColdDrinks", "payload.iced");
        // router.addRecipient("channelHotDrinks", "!payload.iced");
        //
        // return router;
    }

    /**
     * @param orderItem {@link OrderItem}
     * @return {@link Drink}
     */
    @ServiceActivator(inputChannel = "channelColdDrinks", outputChannel = "channelPreparedDrinks")
    public Drink serviceActivatorColdDrinks(final OrderItem orderItem)
    {
        return barista().prepareColdDrink(orderItem);
    }

    /**
     * @param orderItem {@link OrderItem}
     * @return {@link Drink}
     */
    @ServiceActivator(inputChannel = "channelHotDrinks", outputChannel = "channelPreparedDrinks")
    public Drink serviceActivatorHotDrinks(final OrderItem orderItem)
    {
        return barista().prepareHotDrink(orderItem);
    }

    /**
     * @param order {@link Order}
     * @return {@link List}
     */
    @Splitter(inputChannel = "channelOrders", outputChannel = "channelDrinks")
    public List<OrderItem> splitterOrders(final Order order)
    {
        return order.getItems();
    }

    /**
     * @return {@link Waiter}
     */
    @Bean
    public Waiter waiter()
    {
        return new Waiter();
    }
}

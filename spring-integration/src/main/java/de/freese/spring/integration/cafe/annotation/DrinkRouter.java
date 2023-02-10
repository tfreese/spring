package de.freese.spring.integration.cafe.annotation;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Router;

import de.freese.spring.integration.cafe.OrderItem;

/**
 * @author Thomas Freese
 */
@MessageEndpoint
public class DrinkRouter {
    @Router(inputChannel = "drinks")
    public String resolveOrderItemChannel(final OrderItem orderItem) {
        return (orderItem.isIced()) ? "coldDrinks" : "hotDrinks";
    }
}

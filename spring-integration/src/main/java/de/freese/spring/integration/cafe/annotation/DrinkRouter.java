package de.freese.spring.integration.cafe.annotation;

import de.freese.spring.integration.cafe.OrderItem;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Router;

/**
 * @author Thomas Freese
 */
@MessageEndpoint
public class DrinkRouter
{
    @Router(inputChannel = "drinks")
    public String resolveOrderItemChannel(final OrderItem orderItem)
    {
        return (orderItem.isIced()) ? "coldDrinks" : "hotDrinks";
    }
}

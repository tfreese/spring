package de.freese.spring.integration.cafe.annotation;

import java.util.List;

import de.freese.spring.integration.cafe.Order;
import de.freese.spring.integration.cafe.OrderItem;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Splitter;

/**
 * @author Thomas Freese
 */
@MessageEndpoint
public class OrderSplitter
{
    @Splitter(inputChannel = "orders", outputChannel = "drinks")
    public List<OrderItem> split(final Order order)
    {
        return order.getItems();
    }
}

package de.freese.spring.integration.cafe.annotation;

import java.util.List;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Splitter;
import de.freese.spring.integration.cafe.Order;
import de.freese.spring.integration.cafe.OrderItem;

/**
 * @author Thomas Freese
 */
@MessageEndpoint
public class OrderSplitter
{
    /**
     * @param order {@link Order}
     * @return {@link List}
     */
    @Splitter(inputChannel = "orders", outputChannel = "drinks")
    public List<OrderItem> split(final Order order)
    {
        return order.getItems();
    }
}

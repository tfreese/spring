package de.freese.spring.integration.cafe.annotation;

import java.util.List;
import org.springframework.integration.annotation.Aggregator;
import org.springframework.integration.annotation.CorrelationStrategy;
import org.springframework.integration.annotation.MessageEndpoint;
import de.freese.spring.integration.cafe.Delivery;
import de.freese.spring.integration.cafe.Drink;

/**
 * @author Thomas Freese
 */
@MessageEndpoint
public class Waiter extends de.freese.spring.integration.cafe.xml.Waiter
{
    /**
     * @param drink {@link Drink}
     * @return int
     */
    @CorrelationStrategy
    public int correlateByOrderNumber(final Drink drink)
    {
        return drink.getOrderNumber();
    }

    /**
     * @see de.freese.spring.integration.cafe.xml.Waiter#prepareDelivery(java.util.List)
     */
    @Override
    @Aggregator(inputChannel = "preparedDrinks", outputChannel = "deliveries")
    public Delivery prepareDelivery(final List<Drink> drinks)
    {
        return super.prepareDelivery(drinks);
    }
}

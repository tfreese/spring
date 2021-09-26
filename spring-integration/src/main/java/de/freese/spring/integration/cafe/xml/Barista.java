package de.freese.spring.integration.cafe.xml;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.freese.spring.integration.cafe.Drink;
import de.freese.spring.integration.cafe.OrderItem;

/**
 * @author Thomas Freese
 */
public class Barista
{
    /**
     *
     */
    private AtomicInteger coldDrinkCounter = new AtomicInteger();
    /**
     *
     */
    private long coldDrinkDelay = 400;
    /**
     *
     */
    private AtomicInteger hotDrinkCounter = new AtomicInteger();
    /**
     *
     */
    private long hotDrinkDelay = 800;
    /**
     *
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * @param orderItem {@link OrderItem}
     *
     * @return {@link Drink}
     */
    public Drink prepareColdDrink(final OrderItem orderItem)
    {
        try
        {
            Thread.sleep(this.coldDrinkDelay);

            this.logger.info("{} prepared cold drink #{} for order #{}: {}", Thread.currentThread().getName(), this.coldDrinkCounter.incrementAndGet(),
                    orderItem.getOrderNumber(), orderItem);

            return new Drink(orderItem.getOrderNumber(), orderItem.getDrinkType(), orderItem.isIced());
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * @param orderItem {@link OrderItem}
     *
     * @return {@link Drink}
     */
    public Drink prepareHotDrink(final OrderItem orderItem)
    {
        try
        {
            Thread.sleep(this.hotDrinkDelay);

            this.logger.info("{} prepared hot drink #{} for order #{}: {}", Thread.currentThread().getName(), this.hotDrinkCounter.incrementAndGet(),
                    orderItem.getOrderNumber(), orderItem);

            return new Drink(orderItem.getOrderNumber(), orderItem.getDrinkType(), orderItem.isIced());
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * @param coldDrinkDelay long
     */
    public void setColdDrinkDelay(final long coldDrinkDelay)
    {
        this.coldDrinkDelay = coldDrinkDelay;
    }

    /**
     * @param hotDrinkDelay long
     */
    public void setHotDrinkDelay(final long hotDrinkDelay)
    {
        this.hotDrinkDelay = hotDrinkDelay;
    }
}

package de.freese.spring.integration.cafe.xml;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import de.freese.spring.integration.cafe.Drink;
import de.freese.spring.integration.cafe.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Freese
 */
public class Barista
{
    private final AtomicInteger coldDrinkCounter = new AtomicInteger();

    private final AtomicInteger hotDrinkCounter = new AtomicInteger();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private long coldDrinkDelay = 400L;

    private long hotDrinkDelay = 800L;

    public Drink prepareColdDrink(final OrderItem orderItem)
    {
        try
        {
            TimeUnit.MILLISECONDS.sleep(this.coldDrinkDelay);

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

    public Drink prepareHotDrink(final OrderItem orderItem)
    {
        try
        {
            TimeUnit.MILLISECONDS.sleep(this.hotDrinkDelay);

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

    public void setColdDrinkDelay(final long coldDrinkDelay)
    {
        this.coldDrinkDelay = coldDrinkDelay;
    }

    public void setHotDrinkDelay(final long hotDrinkDelay)
    {
        this.hotDrinkDelay = hotDrinkDelay;
    }
}

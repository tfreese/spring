package de.freese.spring.integration.cafe.xml;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.freese.spring.integration.cafe.Drink;
import de.freese.spring.integration.cafe.OrderItem;

/**
 * @author Thomas Freese
 */
public class XmlBarista {
    private final AtomicInteger coldDrinkCounter = new AtomicInteger();
    private final AtomicInteger hotDrinkCounter = new AtomicInteger();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private long coldDrinkDelay = 400L;
    private long hotDrinkDelay = 800L;

    public Drink prepareColdDrink(final OrderItem orderItem) {
        try {
            TimeUnit.MILLISECONDS.sleep(coldDrinkDelay);

            logger.info("{} prepared cold drink #{} for order #{}: {}", Thread.currentThread().getName(), coldDrinkCounter.incrementAndGet(), orderItem.getOrderNumber(),
                    orderItem);

            return new Drink(orderItem.getOrderNumber(), orderItem.getDrinkType(), orderItem.isIced());
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public Drink prepareHotDrink(final OrderItem orderItem) {
        try {
            TimeUnit.MILLISECONDS.sleep(hotDrinkDelay);

            logger.info("{} prepared hot drink #{} for order #{}: {}", Thread.currentThread().getName(), hotDrinkCounter.incrementAndGet(), orderItem.getOrderNumber(),
                    orderItem);

            return new Drink(orderItem.getOrderNumber(), orderItem.getDrinkType(), orderItem.isIced());
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public void setColdDrinkDelay(final long coldDrinkDelay) {
        this.coldDrinkDelay = coldDrinkDelay;
    }

    public void setHotDrinkDelay(final long hotDrinkDelay) {
        this.hotDrinkDelay = hotDrinkDelay;
    }
}

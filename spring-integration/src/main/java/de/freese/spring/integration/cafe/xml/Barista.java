/*
 * Copyright 2002-2010 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed
 * to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.freese.spring.integration.cafe.xml;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.freese.spring.integration.cafe.Drink;
import de.freese.spring.integration.cafe.OrderItem;

/**
 * @author Mark Fisher
 * @author Marius Bogoevici
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
     * Erstellt ein neues {@link Barista} Object.
     */
    public Barista()
    {
        super();
    }

    /**
     * @param orderItem {@link OrderItem}
     * @return {@link Drink}
     */
    public Drink prepareColdDrink(final OrderItem orderItem)
    {
        try
        {
            Thread.sleep(this.coldDrinkDelay);

            this.logger.info(Thread.currentThread().getName() + " prepared cold drink #" + this.coldDrinkCounter.incrementAndGet()
                    + " for order #" + orderItem.getOrderNumber() + ": " + orderItem);

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
     * @return {@link Drink}
     */
    public Drink prepareHotDrink(final OrderItem orderItem)
    {
        try
        {
            Thread.sleep(this.hotDrinkDelay);

            this.logger.info(Thread.currentThread().getName() + " prepared hot drink #" + this.hotDrinkCounter.incrementAndGet()
                    + " for order #" + orderItem.getOrderNumber() + ": " + orderItem);

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

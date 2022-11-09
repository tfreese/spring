// Created: 01.04.2013
package de.freese.spring.integration.cafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Freese
 */
public class DeliveryLogger
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DeliveryLogger.class);

    public void log(final Delivery delivery)
    {
        LOGGER.info("-----------------------");
        LOGGER.info("Order #{}", delivery.getOrderNumber());

        for (Drink drink : delivery.getDeliveredDrinks())
        {
            LOGGER.info("{}", drink);
        }

        LOGGER.info("-----------------------");
    }
}

package de.freese.spring.integration.cafe.annotation;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;
import de.freese.spring.integration.cafe.Drink;
import de.freese.spring.integration.cafe.OrderItem;

/**
 * @author Thomas Freese
 */
@Component
public class Barista extends de.freese.spring.integration.cafe.xml.Barista
{
    /**
     * @see de.freese.spring.integration.cafe.xml.Barista#prepareColdDrink(de.freese.spring.integration.cafe.OrderItem)
     */
    @Override
    @ServiceActivator(inputChannel = "coldDrinkBarista", outputChannel = "preparedDrinks")
    public Drink prepareColdDrink(final OrderItem orderItem)
    {
        return super.prepareColdDrink(orderItem);
    }

    /**
     * @see de.freese.spring.integration.cafe.xml.Barista#prepareHotDrink(de.freese.spring.integration.cafe.OrderItem)
     */
    @Override
    @ServiceActivator(inputChannel = "hotDrinkBarista", outputChannel = "preparedDrinks")
    public Drink prepareHotDrink(final OrderItem orderItem)
    {
        return super.prepareHotDrink(orderItem);
    }
}

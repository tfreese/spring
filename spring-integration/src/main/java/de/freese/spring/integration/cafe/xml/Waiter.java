package de.freese.spring.integration.cafe.xml;

import java.util.List;

import de.freese.spring.integration.cafe.Delivery;
import de.freese.spring.integration.cafe.Drink;

/**
 * @author Thomas Freese
 */
public class Waiter
{
    public Delivery prepareDelivery(final List<Drink> drinks)
    {
        return new Delivery(drinks);
    }
}

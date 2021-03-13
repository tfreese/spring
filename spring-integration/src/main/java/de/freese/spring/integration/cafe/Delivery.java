package de.freese.spring.integration.cafe;

import java.util.List;

/**
 * @author Thomas Freese
 */
public class Delivery
{
    /**
     *
     */
    private final List<Drink> deliveredDrinks;

    /**
     *
     */
    private final int orderNumber;

    /**
     * Erstellt ein neues {@link Delivery} Object.
     *
     * @param deliveredDrinks {@link List}
     */
    public Delivery(final List<Drink> deliveredDrinks)
    {
        super();

        if ((deliveredDrinks == null) || deliveredDrinks.isEmpty())
        {
            throw new IllegalArgumentException("deliveredDrinks required");
        }

        this.deliveredDrinks = deliveredDrinks;
        this.orderNumber = deliveredDrinks.get(0).getOrderNumber();
    }

    /**
     * @return {@link List}
     */
    public List<Drink> getDeliveredDrinks()
    {
        return this.deliveredDrinks;
    }

    /**
     * @return int
     */
    public int getOrderNumber()
    {
        return this.orderNumber;
    }
}

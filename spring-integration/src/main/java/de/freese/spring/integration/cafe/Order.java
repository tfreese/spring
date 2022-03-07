package de.freese.spring.integration.cafe;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Freese
 */
public class Order
{
    /**
     *
     */
    private final int number;
    /**
     *
     */
    private final List<OrderItem> orderItems = new ArrayList<>();

    /**
     * Erstellt ein neues {@link Order} Object.
     *
     * @param number int
     */
    public Order(final int number)
    {
        super();

        this.number = number;
    }

    /**
     * @param drinkType {@link DrinkType}
     * @param iced boolean
     */
    public void addItem(final DrinkType drinkType, final boolean iced)
    {
        this.orderItems.add(new OrderItem(this, drinkType, iced));
    }

    /**
     * @return {@link List}
     */
    public List<OrderItem> getItems()
    {
        return this.orderItems;
    }

    /**
     * @return int
     */
    public int getNumber()
    {
        return this.number;
    }
}

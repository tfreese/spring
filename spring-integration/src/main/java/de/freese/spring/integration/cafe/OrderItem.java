package de.freese.spring.integration.cafe;

/**
 * @author Thomas Freese
 */
public class OrderItem
{
    /**
     *
     */
    private boolean iced;
    /**
     *
     */
    private final Order order;
    /**
     *
     */
    private DrinkType type;

    /**
     * Erstellt ein neues {@link OrderItem} Object.
     *
     * @param order {@link Order}
     * @param type {@link DrinkType}
     * @param iced boolean
     */
    public OrderItem(final Order order, final DrinkType type, final boolean iced)
    {
        super();

        this.order = order;
        this.type = type;
        this.iced = iced;
    }

    /**
     * @return {@link DrinkType}
     */
    public DrinkType getDrinkType()
    {
        return this.type;
    }

    /**
     * @return {@link Order}
     */
    public int getOrderNumber()
    {
        return this.order.getNumber();
    }

    /**
     * @return boolean
     */
    public boolean isIced()
    {
        return this.iced;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return ((this.iced) ? "iced " : "hot ") + this.type;
    }
}

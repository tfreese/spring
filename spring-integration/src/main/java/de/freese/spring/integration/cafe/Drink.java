package de.freese.spring.integration.cafe;

/**
 * @author Thomas Freese
 */
public class Drink
{
    /**
     *
     */
    private final DrinkType drinkType;

    /**
     * 
     */
    private final boolean iced;

    /**
     *
     */
    private final int orderNumber;

    /**
     * Erstellt ein neues {@link Drink} Object.
     * 
     * @param orderNumber int
     * @param drinkType {@link DrinkType}
     * @param iced boolean
     */
    public Drink(final int orderNumber, final DrinkType drinkType, final boolean iced)
    {
        super();

        this.orderNumber = orderNumber;
        this.drinkType = drinkType;
        this.iced = iced;
    }

    /**
     * @return int
     */
    public int getOrderNumber()
    {
        return this.orderNumber;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return (this.iced ? "Iced" : "Hot") + " " + this.drinkType.toString();
    }
}

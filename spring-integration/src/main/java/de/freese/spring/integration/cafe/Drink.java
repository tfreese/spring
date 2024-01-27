package de.freese.spring.integration.cafe;

/**
 * @author Thomas Freese
 */
public class Drink {
    private final DrinkType drinkType;
    private final boolean iced;
    private final int orderNumber;

    public Drink(final int orderNumber, final DrinkType drinkType, final boolean iced) {
        super();

        this.orderNumber = orderNumber;
        this.drinkType = drinkType;
        this.iced = iced;
    }

    public int getOrderNumber() {
        return this.orderNumber;
    }

    @Override
    public String toString() {
        return (this.iced ? "Iced" : "Hot") + " " + this.drinkType.toString();
    }
}

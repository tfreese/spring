package de.freese.spring.integration.cafe;

/**
 * @author Thomas Freese
 */
public class OrderItem {
    private final boolean iced;

    private final Order order;

    private final DrinkType type;

    public OrderItem(final Order order, final DrinkType type, final boolean iced) {
        super();

        this.order = order;
        this.type = type;
        this.iced = iced;
    }

    public DrinkType getDrinkType() {
        return this.type;
    }

    public int getOrderNumber() {
        return this.order.getNumber();
    }

    public boolean isIced() {
        return this.iced;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ((this.iced) ? "iced " : "hot ") + this.type;
    }
}

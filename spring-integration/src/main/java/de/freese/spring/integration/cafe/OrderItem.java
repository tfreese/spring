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
        return type;
    }

    public int getOrderNumber() {
        return order.getNumber();
    }

    public boolean isIced() {
        return iced;
    }

    @Override
    public String toString() {
        return (iced ? "iced " : "hot ") + type;
    }
}

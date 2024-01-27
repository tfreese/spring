package de.freese.spring.integration.cafe;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Freese
 */
public class Order {
    private final int number;
    private final List<OrderItem> orderItems = new ArrayList<>();

    public Order(final int number) {
        super();

        this.number = number;
    }

    public void addItem(final DrinkType drinkType, final boolean iced) {
        this.orderItems.add(new OrderItem(this, drinkType, iced));
    }

    public List<OrderItem> getItems() {
        return this.orderItems;
    }

    public int getNumber() {
        return this.number;
    }
}

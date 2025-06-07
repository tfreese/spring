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
        orderItems.add(new OrderItem(this, drinkType, iced));
    }

    public List<OrderItem> getItems() {
        return orderItems;
    }

    public int getNumber() {
        return number;
    }
}

package de.freese.spring.integration.cafe;

import java.util.List;

/**
 * @author Thomas Freese
 */
public class Delivery {
    private final List<Drink> deliveredDrinks;
    private final int orderNumber;

    public Delivery(final List<Drink> deliveredDrinks) {
        super();

        if (deliveredDrinks == null || deliveredDrinks.isEmpty()) {
            throw new IllegalArgumentException("deliveredDrinks required");
        }

        this.deliveredDrinks = deliveredDrinks;
        orderNumber = deliveredDrinks.getFirst().getOrderNumber();
    }

    public List<Drink> getDeliveredDrinks() {
        return deliveredDrinks;
    }

    public int getOrderNumber() {
        return orderNumber;
    }
}

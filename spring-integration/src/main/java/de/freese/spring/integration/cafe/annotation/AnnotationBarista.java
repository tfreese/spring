package de.freese.spring.integration.cafe.annotation;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

import de.freese.spring.integration.cafe.Drink;
import de.freese.spring.integration.cafe.OrderItem;
import de.freese.spring.integration.cafe.xml.XmlBarista;

/**
 * @author Thomas Freese
 */
@Component
public class AnnotationBarista extends XmlBarista {
    @Override
    @ServiceActivator(inputChannel = "coldDrinkBarista", outputChannel = "preparedDrinks")
    public Drink prepareColdDrink(final OrderItem orderItem) {
        return super.prepareColdDrink(orderItem);
    }

    @Override
    @ServiceActivator(inputChannel = "hotDrinkBarista", outputChannel = "preparedDrinks")
    public Drink prepareHotDrink(final OrderItem orderItem) {
        return super.prepareHotDrink(orderItem);
    }
}

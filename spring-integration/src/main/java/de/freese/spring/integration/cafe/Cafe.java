package de.freese.spring.integration.cafe;

/**
 * Zentraler Einstiegspunkt für Bestellungen.
 *
 * @author Thomas Freese
 */
@FunctionalInterface
public interface Cafe
{
    /**
     * @param order {@link Order}
     */
    void placeOrder(Order order);
}

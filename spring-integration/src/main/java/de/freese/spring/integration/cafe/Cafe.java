package de.freese.spring.integration.cafe;

/**
 * Zentraler Einstiegspunkt für Bestellungen.
 *
 * @author Thomas Freese
 */
@FunctionalInterface
public interface Cafe
{
    void placeOrder(Order order);
}

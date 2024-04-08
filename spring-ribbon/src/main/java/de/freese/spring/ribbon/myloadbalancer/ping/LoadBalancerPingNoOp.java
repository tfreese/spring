// Created: 22.03.2018
package de.freese.spring.ribbon.myloadbalancer.ping;

/**
 * Diese Implementierung liefert immer true.
 *
 * @author Thomas Freese
 */
public class LoadBalancerPingNoOp implements LoadBalancerPing {
    @Override
    public boolean isAlive(final String server) {
        return true;
    }
}

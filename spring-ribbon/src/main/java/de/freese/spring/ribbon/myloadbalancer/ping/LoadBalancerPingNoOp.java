package de.freese.spring.ribbon.myloadbalancer.ping;

/**
 * Diese Implementierung liefert immer true.
 *
 * @author Thomas Freese
 * @since 22.03.2018
 */
public class LoadBalancerPingNoOp implements LoadBalancerPing {
    @Override
    public boolean isAlive(final String server) {
        return true;
    }
}

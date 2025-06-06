// Created: 22.03.2018
package de.freese.spring.ribbon.myloadbalancer.ping;

/**
 * Diese Implementierung liefert immer den gesetzten Wert.<br>
 * Default: true
 *
 * @author Thomas Freese
 */
public class LoadBalancerPingConstant implements LoadBalancerPing {
    private boolean constant = true;

    @Override
    public boolean isAlive(final String server) {
        return constant;
    }

    public boolean isConstant() {
        return constant;
    }

    public void setConstant(final boolean constant) {
        this.constant = constant;
    }
}

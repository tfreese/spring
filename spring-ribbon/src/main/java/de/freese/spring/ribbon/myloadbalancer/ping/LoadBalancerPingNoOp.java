// Created: 22.03.2018
package de.freese.spring.ribbon.myloadbalancer.ping;

/**
 * Diese Implementierung liefert immer true.
 * 
 * @author Thomas Freese
 */
public class LoadBalancerPingNoOp implements LoadBalancerPing
{
    /**
     * Erzeugt eine neue Instanz von {@link LoadBalancerPingNoOp}.
     */
    public LoadBalancerPingNoOp()
    {
        super();
    }

    /**
     * @see de.freese.spring.ribbon.myloadbalancer.ping.LoadBalancerPing#isAlive(java.lang.String)
     */
    @Override
    public boolean isAlive(final String server)
    {
        return true;
    }
}

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
     * @see de.freese.spring.ribbon.myloadbalancer.ping.LoadBalancerPing#isAlive(java.lang.String)
     */
    @Override
    public boolean isAlive(final String server)
    {
        return true;
    }
}

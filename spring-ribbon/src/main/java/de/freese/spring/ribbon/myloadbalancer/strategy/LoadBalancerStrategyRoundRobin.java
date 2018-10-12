// Created: 21.03.2018
package de.freese.spring.ribbon.myloadbalancer.strategy;

import java.util.List;

/**
 * Liefert den nächsten "isAlive"-Server im Round-Robin Verfahren.
 *
 * @author Thomas Freese
 */
public class LoadBalancerStrategyRoundRobin implements LoadBalancerStrategy
{
    /**
     *
     */
    private int index = 0;

    /**
     * Erzeugt eine neue Instanz von {@link LoadBalancerStrategyRoundRobin}.
     */
    public LoadBalancerStrategyRoundRobin()
    {
        super();
    }

    /**
     * @see de.freese.spring.ribbon.myloadbalancer.strategy.LoadBalancerStrategy#chooseServer(java.util.List, java.lang.String)
     */
    @Override
    public String chooseServer(final List<String> server, final String key)
    {
        if (server.isEmpty())
        {
            return null;
        }

        if (this.index >= server.size())
        {
            // Server Liste könnte sich nach Ping geändert haben.
            this.index = 0;
        }

        String host = server.get(this.index++);

        if (this.index == server.size())
        {
            this.index = 0;
        }

        return host;
    }
}

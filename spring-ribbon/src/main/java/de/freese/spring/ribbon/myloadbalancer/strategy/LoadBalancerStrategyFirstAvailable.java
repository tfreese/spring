// Created: 21.03.2018
package de.freese.spring.ribbon.myloadbalancer.strategy;

import java.util.List;

/**
 * Liefert den immer ersten "isAlive"-Server.
 *
 * @author Thomas Freese
 */
public class LoadBalancerStrategyFirstAvailable implements LoadBalancerStrategy
{
    /**
     * Erzeugt eine neue Instanz von {@link LoadBalancerStrategyFirstAvailable}.
     */
    public LoadBalancerStrategyFirstAvailable()
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

        String host = server.get(0);

        return host;
    }
}

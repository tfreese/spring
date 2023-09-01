// Created: 21.03.2018
package de.freese.spring.ribbon.myloadbalancer.strategy;

import java.util.List;

/**
 * Liefert den immer ersten "isAlive"-Server.
 *
 * @author Thomas Freese
 */
public class LoadBalancerStrategyFirstAvailable implements LoadBalancerStrategy {
    @Override
    public String chooseServer(final List<String> server, final String key) {
        return server.get(0);
    }
}

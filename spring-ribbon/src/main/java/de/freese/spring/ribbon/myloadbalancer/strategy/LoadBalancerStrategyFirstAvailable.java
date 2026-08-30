package de.freese.spring.ribbon.myloadbalancer.strategy;

import java.util.List;

/**
 * Liefert den immer ersten "isAlive"-Server.
 *
 * @author Thomas Freese
 * @since 21.03.2018
 */
public class LoadBalancerStrategyFirstAvailable implements LoadBalancerStrategy {
    @Override
    public String chooseServer(final List<String> server, final String key) {
        return server.getFirst();
    }
}

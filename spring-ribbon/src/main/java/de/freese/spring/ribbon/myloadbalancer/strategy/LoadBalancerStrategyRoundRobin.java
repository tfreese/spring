// Created: 21.03.2018
package de.freese.spring.ribbon.myloadbalancer.strategy;

import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * Liefert den nächsten "isAlive"-Server im Round-Robin Verfahren.
 *
 * @author Thomas Freese
 */
public class LoadBalancerStrategyRoundRobin implements LoadBalancerStrategy {
    private static final AtomicIntegerFieldUpdater<LoadBalancerStrategyRoundRobin> NEXT_INDEX = AtomicIntegerFieldUpdater.newUpdater(LoadBalancerStrategyRoundRobin.class,
            "nextIndex");

    private volatile int nextIndex;

    @Override
    public String chooseServer(final List<String> server, final String key) {
        final int length = server.size();

        final int indexToUse = Math.abs(NEXT_INDEX.getAndIncrement(this) % length);

        return server.get(indexToUse);
    }
}

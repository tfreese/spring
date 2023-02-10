// Created: 21.03.2018
package de.freese.spring.ribbon.myloadbalancer.strategy;

import java.util.List;
import java.util.function.BiFunction;

import com.netflix.loadbalancer.IRule;

/**
 * Definiert die Strategie des LoadBalancers, z.B. Round-Robin.<br>
 * Geklaut von com.netflix.loadbalancer.IRule (spring-cloud-starter-netflix-ribbon).
 *
 * @author Thomas Freese
 * @see IRule
 */
@FunctionalInterface
public interface LoadBalancerStrategy extends BiFunction<List<String>, String, String> {
    @Override
    default String apply(List<String> server, String key) {
        return chooseServer(server, key);
    }

    /**
     * Liefert den nächsten Server.
     */
    String chooseServer(List<String> server, String key);
}

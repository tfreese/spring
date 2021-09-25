// Created: 21.03.2018
package de.freese.spring.ribbon.myloadbalancer.strategy;

import java.util.List;

import com.netflix.loadbalancer.IRule;

/**
 * Definiert die Strategie des LoadBalancers, z.B. Round-Robin.<br>
 * Geklaut von com.netflix.loadbalancer.IRule (spring-cloud-starter-netflix-ribbon).
 *
 * @author Thomas Freese
 *
 * @see IRule
 */
@FunctionalInterface
public interface LoadBalancerStrategy
{
    /**
     * Liefert den nächsten Server.
     *
     * @param server {@link List}
     * @param key String
     *
     * @return String
     */
    String chooseServer(List<String> server, String key);
}

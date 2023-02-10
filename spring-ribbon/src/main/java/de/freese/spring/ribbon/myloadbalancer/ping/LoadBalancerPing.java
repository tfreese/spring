// Created: 21.03.2018
package de.freese.spring.ribbon.myloadbalancer.ping;

import com.netflix.loadbalancer.IPing;

/**
 * Interface für einen "isAlive"-Ping auf einen Server.<br>
 * Geklaut von com.netflix.loadbalancer.IPing (spring-cloud-starter-netflix-ribbon).
 *
 * @author Thomas Freese
 * @see IPing
 */
@FunctionalInterface
public interface LoadBalancerPing {
    boolean isAlive(String server);
}

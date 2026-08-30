package de.freese.spring.ribbon.myloadbalancer.ping;

/**
 * Interface für einen "isAlive"-Ping auf einen Server.<br>
 * Geklaut von com.netflix.loadbalancer.IPing (spring-cloud-starter-netflix-ribbon).
 *
 * @author Thomas Freese
 * @since 21.03.2018
 */
@FunctionalInterface
public interface LoadBalancerPing {
    boolean isAlive(String server);
}

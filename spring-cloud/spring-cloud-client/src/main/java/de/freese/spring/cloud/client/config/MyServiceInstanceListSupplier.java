// Created: 10.10.2021
package de.freese.spring.cloud.client.config;

import java.util.List;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Flux;

/**
 * @author Thomas Freese
 * <a href="https://www.baeldung.com/spring-cloud-load-balancer">spring-cloud-load-balancer</a>
 */
class MyServiceInstanceListSupplier implements ServiceInstanceListSupplier {

    private final String serviceId;

    MyServiceInstanceListSupplier(String serviceId) {
        super();

        this.serviceId = serviceId;
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        // @formatter:off
        return Flux.just(List.of(
                new DefaultServiceInstance(serviceId + "-1", serviceId, "127.0.0.1", 8081, false),
                new DefaultServiceInstance(serviceId + "-2", serviceId, "127.0.0.1", 8082, false),
                new DefaultServiceInstance(serviceId + "-3", serviceId, "127.0.0.1", 8083, false)
        ));
        // @formatter:on
    }

    @Override
    public String getServiceId() {
        return this.serviceId;
    }
}

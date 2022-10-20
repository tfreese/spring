// Created: 10.10.2021
package de.freese.spring.cloud.client.config;

import java.util.List;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Flux;

/**
 * @author Thomas Freese
 * @see "https://www.baeldung.com/spring-cloud-load-balancer"
 */
class HelloServiceInstanceListSupplier implements ServiceInstanceListSupplier
{
    public static final String SERVICE_ID = "CLOUD-HELLO-SERVICE";

    private final List<ServiceInstance> instances;

    HelloServiceInstanceListSupplier()
    {
        super();

        this.instances = List.of(
                new DefaultServiceInstance(SERVICE_ID + "-1", SERVICE_ID, "127.0.0.1", 8083, false),
                new DefaultServiceInstance(SERVICE_ID + "-2", SERVICE_ID, "127.0.0.1", 8082, false),
                new DefaultServiceInstance(SERVICE_ID + "-3", SERVICE_ID, "127.0.0.1", 8081, false)
        );
    }

    /**
     * @see java.util.function.Supplier#get()
     */
    @Override
    public Flux<List<ServiceInstance>> get()
    {
        return Flux.just(this.instances);
    }

    /**
     * @see org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier#getServiceId()
     */
    @Override
    public String getServiceId()
    {
        return SERVICE_ID;
    }
}

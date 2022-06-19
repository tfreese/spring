// Created: 10.10.2021
package de.freese.spring.gateway;

import java.util.List;
import java.util.Objects;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Flux;

/**
 * @author Thomas Freese
 * @see "https://www.baeldung.com/spring-cloud-load-balancer"
 */
public class MyServiceInstanceListSupplier implements ServiceInstanceListSupplier
{
    /**
     *
     */
    private final List<ServiceInstance> instances;
    /**
     *
     */
    private final String serviceId;

    /**
     * Erstellt ein neues {@link MyServiceInstanceListSupplier} Object.
     *
     * @param serviceId String
     */
    public MyServiceInstanceListSupplier(final String serviceId)
    {
        super();

        this.serviceId = Objects.requireNonNull(serviceId, "serviceId required");

        // @formatter:off
        this.instances = List.of(
                new DefaultServiceInstance(serviceId + "-1", serviceId, "localhost", 8081, false),
                new DefaultServiceInstance(serviceId + "-2", serviceId, "localhost", 8082, false),
                new DefaultServiceInstance(serviceId + "-3", serviceId, "localhost", 8083, false)
                )
                ;
        // @formatter:on
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
        return this.serviceId;
    }
}

// Created: 10.10.2021
package de.freese.spring.gateway.webclient;

import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.freese.spring.gateway.MyServiceInstanceListSupplier;

/**
 * @author Thomas Freese
 */
@Configuration
public class MyServiceInstanceListSupplierConfig
{
    /**
     * Alternative: Konfiguration in application.yml
     *
     * @param context {@link ConfigurableApplicationContext}
     *
     * @return {@link ServiceInstanceListSupplier}
     */
    @Bean
    // @Primary
    ServiceInstanceListSupplier serviceInstanceListSupplier(final ConfigurableApplicationContext context)
    {
        final ServiceInstanceListSupplier instanceListSupplier = new MyServiceInstanceListSupplier("DATE-SERVICE-MANUELL");

        // return instanceListSupplier;

        // @formatter:off
        return ServiceInstanceListSupplier.builder()
                //.withDiscoveryClient()
                //.withRequestBasedStickySession()
                .withBase(instanceListSupplier)
                //.withBlockingHealthChecks() // RestTemplate muss vorhanden sein !
                .withHealthChecks()
                .build(context)
                ;
        // @formatter:on
    }
}

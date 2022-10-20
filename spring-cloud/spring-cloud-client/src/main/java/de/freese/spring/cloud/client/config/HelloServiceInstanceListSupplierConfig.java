// Created: 10.10.2021
package de.freese.spring.cloud.client.config;

import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * @author Thomas Freese
 */
//@Configuration
public class HelloServiceInstanceListSupplierConfig
{
    /**
     * Alternative zu spring.cloud.discovery.client.simple.instances
     */
    @Bean
    ////@Primary
    ServiceInstanceListSupplier serviceInstanceListSupplier(final ConfigurableApplicationContext context)
    {
        return new HelloServiceInstanceListSupplier();

        //        // @formatter:off
//        return ServiceInstanceListSupplier.builder()
//                //.withDiscoveryClient()
//                .withBase(instanceListSupplier)
//                //.withBlockingHealthChecks() // RestTemplate muss vorhanden sein !
//                //.withHealthChecks() // Funktioniert nicht !
//                //.withHealthChecks(WebClient.builder().build()) // Funktioniert nicht !
//                .withCaching()
//                //.withRequestBasedStickySession()
//                .build(context)
//                ;
//        // @formatter:on
    }
}

// Created: 10.10.2021
package de.freese.spring.gateway.webclient;

import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Thomas Freese
 */
//@Configuration
public class HelloServiceInstanceListSupplierConfig
{
    /**
     * Alternative: Konfiguration in application.yml
     *
     * @param context {@link ConfigurableApplicationContext}
     *
     * @return {@link ServiceInstanceListSupplier}
     */
    //    @Bean
    ////@Primary
    ServiceInstanceListSupplier serviceInstanceListSupplier(final ConfigurableApplicationContext context)
    {
        final ServiceInstanceListSupplier instanceListSupplier = new HelloServiceInstanceListSupplier();

        return instanceListSupplier;

        //        // @formatter:off
//        return ServiceInstanceListSupplier.builder()
//                //.withDiscoveryClient()
//                .withBase(instanceListSupplier)
//                //.withBlockingHealthChecks() // RestTemplate muss vorhanden sein !
//                //.withHealthChecks() // Funktioniert nicht !
//                //.withHealthChecks(WebClient.builder().build()) // Funktioniert nicht !
//                //.withCaching()
//                //.withRequestBasedStickySession()
//                .build(context)
//                ;
//        // @formatter:on
    }
}

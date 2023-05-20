// Created: 10.10.2021
package de.freese.spring.cloud.client.config;

import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Thomas Freese
 * @see "https://www.baeldung.com/spring-cloud-load-balancer"
 */
//@Configuration
public class HelloServiceInstanceListSupplierConfig {
    /**
     * Alternative zu spring.cloud.discovery.client.simple.instances
     */
    @Bean
    ////@Primary
    ServiceInstanceListSupplier serviceInstanceListSupplier(final ConfigurableApplicationContext context, WebClient.Builder webClientBuilder) {
        return new MyServiceInstanceListSupplier("CLOUD-HELLO-SERVICE");

        //        // @formatter:off
//        return ServiceInstanceListSupplier.builder()
//                //.withDiscoveryClient()
//                .withBase(instanceListSupplier)
//                //.withBlockingHealthChecks() // RestTemplate muss vorhanden sein !
//                //.withHealthChecks() // Funktioniert nicht !
//                //.withHealthChecks(webClientBuilder.build()) // Funktioniert nicht !
//                .withCaching()
//                //.withRequestBasedStickySession()
//                .build(context)
//                ;
//        // @formatter:on
    }
}

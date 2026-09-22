package de.freese.spring.cloud.client.config;

import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * <a href="https://www.baeldung.com/spring-cloud-load-balancer">spring-cloud-load-balancer</a>
 *
 * @author Thomas Freese
 * @since 10.10.2021
 */
//@Configuration
public class HelloServiceInstanceListSupplierConfig {
    /**
     * Alternative zu spring.cloud.discovery.client.simple.instances.<br>
     */
    @Bean
    //@Primary
    ServiceInstanceListSupplier serviceInstanceListSupplier(final ConfigurableApplicationContext context, final WebClient.Builder webClientBuilder) {
        return new MyServiceInstanceListSupplier("CLOUD-HELLO-SERVICE");

        // return ServiceInstanceListSupplier.builder()
        //         //.withDiscoveryClient()
        //         .withBase(instanceListSupplier)
        //         //.withBlockingHealthChecks()
        //         //.withHealthChecks()
        //         //.withHealthChecks(webClientBuilder.build())
        //         .withCaching()
        //         //.withRequestBasedStickySession()
        //         .build(context)
        //         ;
    }
}

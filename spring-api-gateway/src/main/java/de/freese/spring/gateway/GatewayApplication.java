// Created: 09.02.2019
package de.freese.spring.gateway;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * https://spring.io/guides/gs/gateway/<br>
 * <br>
 * direkt: curl http://localhost:8081/service/sysdate<br>
 * curl http://localhost:9999/get<br>
 * curl http://localhost:9999/sysdate<br>
 * curl http://localhost:9999/sysdatelb<br>
 * curl http://localhost:9999/sysdatelbman<br>
 * curl --dump-header - --header 'Host: www.circuitbreaker.com' http://localhost:9999/delay/3<br>
 * http://localhost:9999/actuator/gateway/routes/loadbalancer_route
 *
 * @author Thomas Freese
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(UriConfiguration.class)
@RestController
public class GatewayApplication
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        SpringApplication.run(GatewayApplication.class, args);
    }

    /**
     * @return {@link Mono}
     */
    @RequestMapping("/fallback")
    public Mono<String> fallback()
    {
        return Mono.just("fallback").map(r -> r + "\n");
    }

    /**
     * Alternative: Konfiguration in application.yml
     *
     * @param context {@link ConfigurableApplicationContext}
     *
     * @return {@link ServiceInstanceListSupplier}
     */
    @Bean
    public ServiceInstanceListSupplier manuellServiceInstanceListSupplier(final ConfigurableApplicationContext context)
    {
        final String serviceId = "DATE-SERVICE-MANUELL";

        // @formatter:off
        final List<ServiceInstance> instances = List.of(
                new DefaultServiceInstance(serviceId + "-1", serviceId, "localhost", 8081, false),
                new DefaultServiceInstance(serviceId + "-2", serviceId, "localhost", 8082, false),
                new DefaultServiceInstance(serviceId + "-3", serviceId, "localhost", 8083, false)
                )
                ;
        // @formatter:on

        final ServiceInstanceListSupplier instanceListSupplier = new ServiceInstanceListSupplier()
        {
            @Override
            public Flux<List<ServiceInstance>> get()
            {
                return Flux.just(instances);
            }

            @Override
            public String getServiceId()
            {
                return serviceId;
            }
        };

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

    /**
     * Routes werden in der application.yml konfiguriert
     *
     * @param builder {@link RouteLocatorBuilder}
     * @param uriConfiguration {@link UriConfiguration}
     *
     * @return {@link RouteLocator}
     */
    @Bean
    public RouteLocator myRoutes(final RouteLocatorBuilder builder, final UriConfiguration uriConfiguration)
    {
        String httpUri = uriConfiguration.getHttpbin();

        // @formatter:off
        return builder.routes()
                .route(p -> p.path("/get")
                        .filters(f -> f.addRequestHeader("Hello", "World"))
                        .customize(asyncBuilder -> asyncBuilder.id("addrequestheader_route"))
                        .uri(httpUri)
                      )
                .route(p -> p.host("*.circuitbreaker.com")
                        .filters(f -> f.circuitBreaker(config -> config.setName("mycmd")
                                        .setFallbackUri("forward:/fallback")))
                        .customize(asyncBuilder -> asyncBuilder.id("circuitbreaker_route"))
                        .uri(httpUri)
                      )
                .route(p -> p.path("/sysdate")
//                        .filters(f -> f.rewritePath("/sysdate/(?<segment>.*)", "/service/sysdate/${segment}"))
                        .filters(f -> f.rewritePath("/sysdate", "/service/sysdate"))
                        .customize(asyncBuilder -> asyncBuilder.id("rewritepath_route"))
                        .uri("http://localhost:8081")
                      )
                .route(p -> p.path("/sysdatelb")
                      .filters(f -> f.rewritePath("/sysdatelb", "/service/sysdate"))
                      .customize(asyncBuilder -> asyncBuilder.id("loadbalancer_route"))
                      .uri("lb://DATE-SERVICE")
                    )
                .route(p -> p.path("/sysdatelbman")
                        .filters(f -> f.rewritePath("/sysdatelbman", "/service/sysdate"))
                        .customize(asyncBuilder -> asyncBuilder.id("loadbalancer_route_manuell"))
                        .uri("lb://DATE-SERVICE-MANUELL")
                      )
                .build();
        // @formatter:on
    }
}

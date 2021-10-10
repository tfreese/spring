// Created: 09.02.2019
package de.freese.spring.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * Routes werden in der application.yml konfiguriert
     *
     * @param builder {@link RouteLocatorBuilder}
     * @param uriConfiguration {@link UriConfiguration}
     *
     * @return {@link RouteLocator}
     */
    @Bean
    RouteLocator myRoutes(final RouteLocatorBuilder builder, final UriConfiguration uriConfiguration)
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
                      .uri("lb://DATE-SERVICE") // Kommt von Eureka
                    )
                .route(p -> p.path("/sysdatelbman")
                        .filters(f -> f.rewritePath("/sysdatelbman", "/service/sysdate"))
                        .customize(asyncBuilder -> asyncBuilder.id("loadbalancer_route_manuell"))
                        .uri("lb://DATE-SERVICE-MANUELL") // Kommt von MyServiceInstanceListSupplierConfig
                      )
                .build();
        // @formatter:on
    }
}

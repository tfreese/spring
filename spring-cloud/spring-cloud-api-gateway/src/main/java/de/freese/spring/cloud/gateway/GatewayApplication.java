// Created: 09.02.2019
package de.freese.spring.cloud.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * <a href="https://spring.io/guides/gs/gateway/">gateway</a><br>
 * <br>
 * curl http://localhost:8081/service/sysdate<br>
 * curl http://localhost:8091/get<br>
 * curl http://localhost:8091/hello<br>
 * curl http://localhost:8091/lb<br>
 * curl --dump-header - --header 'Host: www.circuitbreaker.com' http://localhost:8091/delay/3<br>
 * curl http://localhost:8091/actuator/gateway/routes/loadbalancer_route<br>
 * <br>
 * Only these are working with Docker:<br>
 * curl http://localhost:8091/lb<br>
 * curl --dump-header - --header 'Host: www.circuitbreaker.com' http://localhost:8091/delay/3<br>
 * <br>
 *
 * @author Thomas Freese
 */
@SpringBootApplication
@EnableConfigurationProperties(UriConfiguration.class)
@RestController
public class GatewayApplication {
    static void main(final String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @GetMapping("/fallback")
    public Mono<String> fallback() {
        return Mono.just("fallback").map(r -> r + System.lineSeparator());
    }

    /**
     * Static Routes are configured in application.yml.
     */
    @Bean
    RouteLocator myRoutes(final RouteLocatorBuilder builder, final UriConfiguration uriConfiguration) {
        final String httpUri = uriConfiguration.getHttpbin();

        return builder.routes()
                .route(p -> p.path("/get")
                        .filters(f -> f.addRequestHeader("Hello", "World"))
                        .customize(asyncBuilder -> asyncBuilder.id("addrequestheader_route"))
                        .uri(httpUri)
                )
                .route(p -> p.host("*.circuitbreaker.com")
                        .filters(f ->
                                f.circuitBreaker(config -> config.setName("mycmd")
                                        .setFallbackUri("forward:/fallback"))
                        )
                        .customize(asyncBuilder -> asyncBuilder.id("circuitbreaker_route"))
                        .uri(httpUri)
                )
                .route(p -> p.path("/hello/**")
                        .filters(f -> f.rewritePath("/hello(?<segment>/?.*)", "/${segment}"))
                        .customize(asyncBuilder -> asyncBuilder.id("rewritepath_route"))
                        .uri("http://localhost:8081")
                )
                .route(p -> p.path("/lb/**")
                        .filters(f -> f.rewritePath("/lb", "/"))
                        .customize(asyncBuilder -> asyncBuilder.id("loadbalancer_route"))
                        .uri("lb://CLOUD-HELLO-SERVICE") // Kommt von Eureka
                )
                .build();
    }
}

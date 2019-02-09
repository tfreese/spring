/**
 * Created: 09.02.2019
 */

package de.freese.spring.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * curl http://localhost:9999/get<br>
 * curl --dump-header - --header 'Host: www.hystrix.com' http://localhost:9999/delay/3
 * 
 * @author Thomas Freese
 */
@SpringBootApplication
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
     * Erstellt ein neues {@link GatewayApplication} Object.
     */
    public GatewayApplication()
    {
        super();
    }

    /**
     * @return {@link Mono}
     */
    @RequestMapping("/fallback")
    public Mono<String> fallback()
    {
        return Mono.just("fallback");
    }

    /**
     * @param builder {@link RouteLocatorBuilder}
     * @param uriConfiguration {@link UriConfiguration}
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
                        .uri(httpUri))
                .route(p -> p.host("*.hystrix.com")
                        .filters(f -> f.hystrix(config -> config.setName("mycmd")
                                        .setFallbackUri("forward:/fallback")))
                        .uri(httpUri))
                .build();
        // @formatter:on
    }
}

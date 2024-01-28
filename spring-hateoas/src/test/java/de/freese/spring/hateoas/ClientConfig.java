// Created: 29.11.2021
package de.freese.spring.hateoas;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.config.HypermediaRestTemplateConfigurer;
import org.springframework.web.client.RestTemplate;

/**
 * @author Thomas Freese
 */
// @TestConfiguration
class ClientConfig {
    ClientConfig() {
        super();

        // System.setProperty("server.port", Integer.toString(SocketUtils.findAvailableTcpPort()));
    }

    @Bean
    public RestTemplate restTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    /**
     * "@Value("${local.server.port}") final int port"
     */
    @Bean
    RestTemplateBuilder restTemplateBuilder(@Value("${server.address:localhost}") final String host, @Value("${server.port}") final int port,
                                            @Value("${server.servlet.context-path:}") final String contextPath) {
        // "http://localhost:" + this.port + this.contextPath + "/greeter/"
        return new RestTemplateBuilder().rootUri("http://" + host + ":" + port + contextPath);
    }

    @Bean
    RestTemplateCustomizer restTemplateCustomizer(final HypermediaRestTemplateConfigurer configurer) {
        return configurer::registerHypermediaTypes;
    }

    // @Bean
    // WebClientCustomizer webClientCustomizer(final HypermediaWebClientConfigurer configurer)
    // {
    // return webClientBuilder -> configurer.registerHypermediaTypes(webClientBuilder);
    // }
}

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
class ClientConfig
{
    /**
     * Erstellt ein neues {@link ClientConfig} Object.
     */
    ClientConfig()
    {
        super();

        // System.setProperty("server.port", Integer.toString(SocketUtils.findAvailableTcpPort()));
    }

    /**
     * @param restTemplateBuilder {@link RestTemplateBuilder}
     *
     * @return {@link RestTemplate}
     */
    @Bean
    public RestTemplate restTemplate(final RestTemplateBuilder restTemplateBuilder)
    {
        return restTemplateBuilder.build();
    }

    /**
     * @param host String
     * @param port int
     * @param contextPath String
     *
     * @return {@link RestTemplateBuilder}
     *
     * @Value("${local.server.port}") final int port,
     */
    @Bean
    RestTemplateBuilder restTemplateBuilder(@Value("${server.address:localhost}") final String host, @Value("${server.port}") final int port,
                                            @Value("${server.servlet.context-path:}") final String contextPath)
    {
        // "http://localhost:" + this.port + this.contextPath + "/greeter/"
        return new RestTemplateBuilder().rootUri("http://" + host + ":" + port + contextPath);
    }

    /**
     * @param configurer {@link HypermediaRestTemplateConfigurer}
     *
     * @return RestTemplateCustomizer
     */
    @Bean
    RestTemplateCustomizer restTemplateCustomizer(final HypermediaRestTemplateConfigurer configurer)
    {
        return configurer::registerHypermediaTypes;
    }

    // /**
    // * @param configurer {@link HypermediaWebClientConfigurer}
    // *
    // * @return {@link WebClientCustomizer}
    // */
    // @Bean
    // WebClientCustomizer webClientCustomizer(final HypermediaWebClientConfigurer configurer)
    // {
    // return webClientBuilder -> configurer.registerHypermediaTypes(webClientBuilder);
    // }
}

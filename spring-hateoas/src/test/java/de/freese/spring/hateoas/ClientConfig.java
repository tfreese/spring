package de.freese.spring.hateoas;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.client.RestClient;

/**
 * @author Thomas Freese
 * @since 29.11.2021
 */
// @TestConfiguration
class ClientConfig {
    ClientConfig() {
        super();

        // System.setProperty("server.port", Integer.toString(SocketUtils.findAvailableTcpPort()));
    }

    @Bean
    public RestClient restClient(final RestClient.Builder restClientBuilder) {
        return restClientBuilder.build();
    }

    /**
     * "@Value("${local.server.port}") final int port"
     */
    @Bean
    RestClient.Builder restClientBuilder(@Value("${server.address:localhost}") final String host, @Value("${server.port}") final int port,
                                         @Value("${server.servlet.context-path:}") final String contextPath) {
        // "http://localhost:" + this.port + this.contextPath + "/greeter/"
        return RestClient.builder().baseUrl("http://" + host + ":" + port + contextPath);
    }

    @Bean
    RestClientCustomizer restClientCustomizer(final HttpMessageConverters messageConverters) {
        return builder -> {
            // Holt alle registrierten Konverter (inkl. HAL/HATEOAS) und übergibt sie dem RestClient.
            builder.configureMessageConverters(converters ->
                    messageConverters.forEach(converters::addCustomConverter)
            );
        };
    }

    // @Bean
    // RestClientCustomizer restClientCustomizer(final HypermediaRestTemplateConfigurer configurer) {
    //     return restClientBuilder -> {
    //         // Temporäres RestTemplate erstellen, um die Hypermedia-Konfiguration aufzuprägen.
    //         final RestTemplate temporaryRestTemplate = new RestTemplate();
    //         configurer.registerHypermediaTypes(temporaryRestTemplate);
    //
    //         // Die registrierten MessageConverter (z. B. für HAL-JSON) in den RestClient-Builder übertragen.
    //         restClientBuilder.configureMessageConverters(converters ->
    //                 temporaryRestTemplate.getMessageConverters().forEach(converters::addCustomConverter)
    //         );
    //     };
    // }

    // @Bean
    // WebClientCustomizer webClientCustomizer(final HypermediaWebClientConfigurer configurer) {
    // return webClientBuilder -> configurer.registerHypermediaTypes(webClientBuilder);
    // }
}

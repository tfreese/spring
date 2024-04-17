package de.freese.spring.data.jpa;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.JdkClientHttpConnector;
import org.springframework.test.web.reactive.server.WebTestClientConfigurer;

@Configuration
public class WebConfig {
    @Bean
    ClientHttpConnector clientHttpConnector() {
        return new JdkClientHttpConnector();
    }

    @Bean
    ClientHttpRequestFactory clientHttpRequestFactory() {
        return new JdkClientHttpRequestFactory();
    }

    @Bean
    RestClientCustomizer restClientCustomizer(final ClientHttpRequestFactory clientHttpRequestFactory) {
        return restClientBuilder -> restClientBuilder.requestFactory(clientHttpRequestFactory);
    }

    @Bean
    RestTemplateCustomizer restTemplateCustomizer(final ClientHttpRequestFactory clientHttpRequestFactory) {
        return restTemplate -> restTemplate.setRequestFactory(clientHttpRequestFactory);
    }

    @Bean
    WebClientCustomizer webClientCustomizer(final ClientHttpConnector clientHttpConnector) {
        return webClientBuilder -> webClientBuilder.clientConnector(clientHttpConnector);
    }

    @Bean
    WebTestClientConfigurer webTestClientConfigurer(final ClientHttpConnector clientHttpConnector) {
        return (builder, httpHandlerBuilder, connector) -> builder.clientConnector(clientHttpConnector);
    }
}

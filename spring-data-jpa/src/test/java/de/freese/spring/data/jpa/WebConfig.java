package de.freese.spring.data.jpa;

import java.time.Duration;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.boot.restclient.RestTemplateCustomizer;
import org.springframework.boot.webclient.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;
import org.springframework.test.web.reactive.server.WebTestClientConfigurer;

/**
 * HttpClientAutoConfiguration
 */
@Configuration
public class WebConfig {

    @Bean
    ClientHttpConnector clientHttpConnector(final CloseableHttpAsyncClient closeableHttpAsyncClient) {
        // return new JdkClientHttpConnector();
        return new HttpComponentsClientHttpConnector(closeableHttpAsyncClient);
    }

    @Bean
    ClientHttpRequestFactory clientHttpRequestFactory(final CloseableHttpClient closeableHttpClient) {
        // return new JdkClientHttpRequestFactory();
        return new HttpComponentsClientHttpRequestFactory(closeableHttpClient);
    }

    @Bean(destroyMethod = "close")
    CloseableHttpAsyncClient closeableHttpAsyncClient() {
        return HttpAsyncClients.createDefault();
    }

    @Bean(destroyMethod = "close")
    CloseableHttpClient closeableHttpClient() {
        // return HttpClients.createDefault();
        return ApacheHttpClientConfigurer.createCloseableHttpClient(3, Duration.ofSeconds(3), null);
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

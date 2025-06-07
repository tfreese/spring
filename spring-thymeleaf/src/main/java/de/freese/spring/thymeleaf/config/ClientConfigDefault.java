// Created: 14.09.2018
package de.freese.spring.thymeleaf.config;

import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableScheduling
@Profile("!with-ssl")
public class ClientConfigDefault {
    @Bean
    public HttpClient httpClient(final PoolingHttpClientConnectionManager poolingConnectionManager) {
        return HttpClientBuilder.create()
                .setConnectionManager(poolingConnectionManager)
                .evictExpiredConnections()
                .setUserAgent("My Java App")
                .build()
                ;
    }

    @Bean
    public PoolingHttpClientConnectionManager poolingConnectionManager() {
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setConnectionFactory(new ManagedHttpClientConnectionFactory())
                .setConnPoolPolicy(PoolReusePolicy.FIFO)
                .build();
    }

    @Bean
    public RestTemplateCustomizer restTemplateCustomizer(final HttpClient httpClient) {
        final HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return restTemplate -> restTemplate.setRequestFactory(httpRequestFactory);
    }

    @Bean
    public WebClientCustomizer webClientCustomizer() {
        // ExchangeStrategies strategies = ExchangeStrategies.builder()
        //         .codecs(configurer -> {
        //             configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
        //             configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
        //
        //         }).build();

        final CloseableHttpAsyncClient client = HttpAsyncClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(3000, TimeUnit.MILLISECONDS)
                        .setResponseTimeout(3000, TimeUnit.MILLISECONDS)
                        .build()
                )
                .build();
        final ClientHttpConnector connector = new HttpComponentsClientHttpConnector(client);

        return webClientBuilder -> webClientBuilder.defaultHeader("user-agent", "web-client")
                //.exchangeStrategies(strategies)
                .clientConnector(connector);
    }
}

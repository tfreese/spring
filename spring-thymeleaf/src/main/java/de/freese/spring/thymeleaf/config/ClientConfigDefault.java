// Created: 14.09.2018
package de.freese.spring.thymeleaf.config;

import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
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
        final ConnectionKeepAliveStrategy connectionKeepAliveStrategy = new DefaultConnectionKeepAliveStrategy() {
            @Override
            public TimeValue getKeepAliveDuration(final HttpResponse response, final HttpContext context) {
                final TimeValue duration = super.getKeepAliveDuration(response, context);

                return duration.getDuration() == -1L ? TimeValue.ofMilliseconds(20) : duration;
            }
        };

        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(3000, TimeUnit.MILLISECONDS)
                .setResponseTimeout(3000, TimeUnit.MILLISECONDS)
                .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(poolingConnectionManager)
                .setKeepAliveStrategy(connectionKeepAliveStrategy)
                .setUserAgent("My Java App")
                .build()
                ;
    }

    @Bean
    public PoolingHttpClientConnectionManager poolingConnectionManager() {
        final Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory())
                .register("https", new SSLConnectionSocketFactory(SSLContexts.createDefault(), new NoopHostnameVerifier()))
                .build();

        final ConnectionConfig connectionConfig = ConnectionConfig.custom().setConnectTimeout(3000, TimeUnit.MILLISECONDS).build();

        final PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        poolingConnectionManager.setDefaultConnectionConfig(connectionConfig);
        poolingConnectionManager.setMaxTotal(50);

        return poolingConnectionManager;
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
        //             configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(this.objectMapper, MediaType.APPLICATION_JSON));
        //             configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(this.objectMapper, MediaType.APPLICATION_JSON));
        //
        //         }).build();

        final HttpAsyncClientBuilder clientBuilder = HttpAsyncClients.custom();

        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(3000, TimeUnit.MILLISECONDS)
                .setResponseTimeout(3000, TimeUnit.MILLISECONDS)
                .build();

        clientBuilder.setDefaultRequestConfig(requestConfig);
        final CloseableHttpAsyncClient client = clientBuilder.build();
        final ClientHttpConnector connector = new HttpComponentsClientHttpConnector(client);

        return webClientBuilder -> webClientBuilder.defaultHeader("user-agent", "web-client")
                //.exchangeStrategies(strategies)
                .clientConnector(connector);
    }
}

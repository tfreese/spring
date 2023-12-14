// Created: 14.09.2018
package de.freese.spring.thymeleaf.config;

import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableScheduling
@Profile("!with-ssl")
public class HttpClientConfigDefault {
    private static final int MAX_TOTAL_CONNECTIONS = 50;

    @Bean
    public HttpClient httpClient(final PoolingHttpClientConnectionManager poolingConnectionManager) throws Exception {
        // @formatter:off
        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(3000, TimeUnit.MILLISECONDS)
                .setResponseTimeout(3000, TimeUnit.MILLISECONDS)
                .build()
                ;

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(poolingConnectionManager)
                .setUserAgent("My Java App")
                .build()
                ;
        // @formatter:on
    }

    @Bean
    public PoolingHttpClientConnectionManager poolingConnectionManager() throws Exception {
        // @formatter:off
        final Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory())
                .register("https", new SSLConnectionSocketFactory(SSLContexts.createDefault(), new NoopHostnameVerifier()))
                .build()
                ;
        // @formatter:on

        final ConnectionConfig connectionConfig = ConnectionConfig.custom().setConnectTimeout(3000, TimeUnit.MILLISECONDS).build();

        final PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        poolingConnectionManager.setDefaultConnectionConfig(connectionConfig);
        poolingConnectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);

        return poolingConnectionManager;
    }
}

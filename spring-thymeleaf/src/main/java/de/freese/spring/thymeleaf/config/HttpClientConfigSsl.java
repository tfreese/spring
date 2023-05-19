// Created: 14.09.2018
package de.freese.spring.thymeleaf.config;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.ssl.PrivateKeyStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.ResourceUtils;

/**
 * @author Thomas Freese
 */
@Configuration
@Profile("with-ssl")
public class HttpClientConfigSsl {
    public static final Logger LOGGER = LoggerFactory.getLogger(HttpClientConfigSsl.class);

    @Bean
    public HttpClient httpClient(final PoolingHttpClientConnectionManager poolingConnectionManager) throws Exception {
        ConnectionKeepAliveStrategy connectionKeepAliveStrategy = new DefaultConnectionKeepAliveStrategy() {
            @Override
            public TimeValue getKeepAliveDuration(final HttpResponse response, final HttpContext context) {
                TimeValue duration = super.getKeepAliveDuration(response, context);

                return duration.getDuration() == -1L ? TimeValue.ofMilliseconds(20) : duration;
            }
        };

        // @formatter:off
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(3000, TimeUnit.MILLISECONDS)
                .setResponseTimeout(3000, TimeUnit.MILLISECONDS)
                .build()
                ;

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(poolingConnectionManager)
                .setKeepAliveStrategy(connectionKeepAliveStrategy)
                .setUserAgent("My secured Java App")
                .build()
                ;
        // @formatter:on
    }

    /**
     * Scheduled Methoden dürfen keine Parameter haben !
     */
    @Bean
    public Runnable idleConnectionMonitor(final PoolingHttpClientConnectionManager poolingConnectionManager) {
        return new Runnable() {
            @Override
            @Scheduled(initialDelay = 10 * 1000, fixedDelay = 10 * 1000) // Alle 10 Sekunden
            // initialDelayString = #{ T(java.lang.Math).random() * 10 }
            // @Scheduled(cron = "0 0 * * * MON-FRI") // Jede Stunde von Montag - Freitag
            // @Scheduled(cron = "4,9,14,19,24,29,34,39,44,49,55,59 * * * *") // Alle 5 Minuten
            // @Scheduled(cron = "0 */15 * * * MON-FRI") // Alle 15 Minuten
            // @Async("executorService")
            public void run() {
                try {
                    if (poolingConnectionManager != null) {
                        LOGGER.debug("idleConnectionMonitor - Closing expired and idle connections...");
                        poolingConnectionManager.closeExpired();
                        poolingConnectionManager.closeIdle(TimeValue.ofSeconds(30));
                    }
                    else {
                        LOGGER.debug("idleConnectionMonitor - Http Client Connection manager is not initialised");
                    }
                }
                catch (Exception ex) {
                    String message = String.format("idleConnectionMonitor - Exception occurred. msg = %s", ex.getMessage());
                    LOGGER.error(message, ex);
                }
            }
        };
    }

    @Bean
    public PoolingHttpClientConnectionManager poolingConnectionManager(final SSLContext sslContext) throws Exception {
        // @formatter:off
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory())
                .register("https", new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier()))
                .build()
                ;
        // @formatter:on

        ConnectionConfig connectionConfig = ConnectionConfig.custom().setConnectTimeout(3000, TimeUnit.MILLISECONDS).build();

        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        poolingConnectionManager.setDefaultConnectionConfig(connectionConfig);
        poolingConnectionManager.setMaxTotal(50);

        return poolingConnectionManager;
    }

    @Bean
    public SSLContext sslContext() throws Exception {
        PrivateKeyStrategy privateKeyStrategy = (aliases, socket) -> {
            LOGGER.debug("{}", aliases);
            return "server";
        };

        TrustStrategy trustStrategy = new TrustAllStrategy(); // (chain, authType) -> true;

        char[] keyStorePassword = "password".toCharArray();
        char[] certPassword = "password".toCharArray();
        char[] trustStorePassword = "password".toCharArray();

        // @formatter:off
        return SSLContextBuilder.create()
                .setKeyStoreType("PKCS12")
                .setProtocol("TLSv1.3")
                .setSecureRandom(new SecureRandom())
                .loadKeyMaterial(ResourceUtils.getFile("classpath:server_keystore.p12"), keyStorePassword, certPassword, privateKeyStrategy)
                .loadTrustMaterial(ResourceUtils.getFile("classpath:server_truststore.p12"), trustStorePassword, trustStrategy)
                .build()
                ;
        // @formatter:on
    }
}

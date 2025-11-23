// Created: 14.09.2018
package de.freese.spring.thymeleaf.config;

import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.restclient.RestTemplateCustomizer;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.webclient.WebClientCustomizer;
import org.springframework.boot.webclient.autoconfigure.WebClientSsl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author Thomas Freese
 */
@Configuration
@Profile("with-ssl")
public class ClientConfigSsl {
    public static final Logger LOGGER = LoggerFactory.getLogger(ClientConfigSsl.class);

    @Bean
    public HttpClient httpClient(final PoolingHttpClientConnectionManager poolingConnectionManager) {
        return HttpClients.custom()
                .setConnectionManager(poolingConnectionManager)
                .evictExpiredConnections()
                .setUserAgent("My secured Java App")
                .build()
                ;
    }

    /**
     * Scheduled Methods must have no Parameters!
     */
    @Bean
    public Runnable idleConnectionMonitor(final PoolingHttpClientConnectionManager poolingConnectionManager) {
        return new Runnable() {
            @Override
            @Scheduled(initialDelay = 10 * 1000, fixedDelay = 10 * 1000) // 10 Seconds
            // initialDelayString = #{ T(java.lang.Math).random() * 10 }
            // @Scheduled(cron = "0 0 * * * MON-FRI") // Every Hour from Monday - Friday
            // @Scheduled(cron = "4,9,14,19,24,29,34,39,44,49,55,59 * * * *") // Every 5 Minutes
            // @Scheduled(cron = "0 */15 * * * MON-FRI") // Every 15 Minutes
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
                    final String message = String.format("idleConnectionMonitor - Exception occurred. msg = %s", ex.getMessage());
                    LOGGER.error(message, ex);
                }
            }
        };
    }

    @Bean
    public PoolingHttpClientConnectionManager poolingConnectionManager(final SslBundles sslBundles) {
        final TlsSocketStrategy tlsSocketStrategy = new DefaultClientTlsStrategy(sslBundles.getBundle("web-client").createSslContext(), new NoopHostnameVerifier());

        return PoolingHttpClientConnectionManagerBuilder.create()
                .setConnectionFactory(new ManagedHttpClientConnectionFactory())
                .setTlsSocketStrategy(tlsSocketStrategy)
                .build();
    }

    @Bean
    public RestTemplateCustomizer restTemplateCustomizer(final HttpClient httpClient) {
        final HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return restTemplate -> restTemplate.setRequestFactory(httpRequestFactory);
    }

    @Bean
    public WebClientCustomizer webClientCustomizer(final WebClientSsl webClientSsl) {
        // ExchangeStrategies strategies = ExchangeStrategies.builder()
        //         .codecs(configurer -> {
        //             configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
        //             configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
        //
        //         }).build();

        // final SslContext sslContext = SslContextBuilder.forClient().trustManager(trustManagerFactory).build();

        // ClientHttpConnector httpConnector = new ReactorClientHttpConnector(opt -> opt.sslContext(sslContext));
        // HttpClient httpClient = HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        // webClientBuilder.baseUrl(rootUri).clientConnector(new ReactorClientHttpConnector(httpClient))

        final CloseableHttpAsyncClient client = HttpAsyncClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(3000, TimeUnit.MILLISECONDS)
                        .setResponseTimeout(3000, TimeUnit.MILLISECONDS)
                        .build()
                )
                .build();
        final ClientHttpConnector connector = new HttpComponentsClientHttpConnector(client);

        return webClientBuilder -> webClientBuilder
                .defaultHeader("user-agent", "web-client")
                //.exchangeStrategies(strategies)
                .clientConnector(connector)
                .apply(webClientSsl.fromBundle("web-client")) // SSL as last !!!
                ;
    }

    // @Bean
    // public SSLContext sslContext() throws Exception {
    //     final PrivateKeyStrategy privateKeyStrategy = (aliases, socket) -> {
    //         LOGGER.debug("{}", aliases);
    //         return "server";
    //     };
    //
    //     final TrustStrategy trustStrategy = new TrustAllStrategy(); // (chain, authType) -> true;
    //
    //     final char[] keyStorePassword = "password".toCharArray();
    //     final char[] certPassword = "password".toCharArray();
    //     final char[] trustStorePassword = "password".toCharArray();
    //
    //     return SSLContextBuilder.create()
    //             .setKeyStoreType("PKCS12")
    //             .setProtocol("TLSv1.3")
    //             .setSecureRandom(new SecureRandom())
    //             .loadKeyMaterial(ResourceUtils.getFile("classpath:client_keystore.p12"), keyStorePassword, certPassword, privateKeyStrategy)
    //             .loadTrustMaterial(ResourceUtils.getFile("classpath:client_truststore.p12"), trustStorePassword, trustStrategy)
    //             .build()
    //             ;
    // }

    // /**
    // * @param sslContext {@link SSLContext}
    // * @return {@link HttpComponentsClientHttpRequestFactory}
    // * @throws Exception Falls was schief geht.
    // */
    // @SuppressWarnings("resource")
    // public HttpComponentsClientHttpRequestFactory createHttpComponentsClientHttpRequestFactory(final SslBundles sslBundles) throws Exception {
    // final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslBundles.getBundle("web-server").createSslContext(), new NoopHostnameVerifier());
    //
    // final CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(sslsf).build();
    //
    // final HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(client);
    // httpRequestFactory.setReadTimeout(3000);
    // httpRequestFactory.setConnectTimeout(3000);
    //
    // // restTemplateBuilder = this.restTemplateBuilder.requestFactory(() -> httpRequestFactory);
    //
    // return httpRequestFactory;
    // }
}

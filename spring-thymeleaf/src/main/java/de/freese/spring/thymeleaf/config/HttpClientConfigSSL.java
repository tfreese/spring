/**
 * Created: 14.09.2018
 */

package de.freese.spring.thymeleaf.config;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.PrivateKeyStrategy;
import org.apache.http.ssl.SSLContextBuilder;
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
public class HttpClientConfigSSL
{
    /**
    *
    */
    private static final int CLOSE_IDLE_CONNECTION_WAIT_TIME_SECS = 30;

    /**
    *
    */
    private static final int DEFAULT_KEEP_ALIVE_TIME_MILLIS = 20 * 1000;

    /**
    *
    */
    public static final Logger LOGGER = LoggerFactory.getLogger(HttpClientConfigSSL.class);

    /**
    *
    */
    private static final int MAX_TOTAL_CONNECTIONS = 50;

    /**
     * Erstellt ein neues {@link HttpClientConfigSSL} Object.
     */
    public HttpClientConfigSSL()
    {
        super();
    }

    /**
     * @param poolingConnectionManager {@link PoolingHttpClientConnectionManager}
     * @return {@link HttpClient}
     * @throws Exception Falls was schief geht.
     */
    @Bean
    public HttpClient httpClient(final PoolingHttpClientConnectionManager poolingConnectionManager) throws Exception
    {
        ConnectionKeepAliveStrategy connectionKeepAliveStrategy = new DefaultConnectionKeepAliveStrategy()
        {
            /**
             * @see org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy#getKeepAliveDuration(org.apache.http.HttpResponse,
             *      org.apache.http.protocol.HttpContext)
             */
            @Override
            public long getKeepAliveDuration(final HttpResponse response, final HttpContext context)
            {
                long duration = super.getKeepAliveDuration(response, context);

                return duration == -1 ? DEFAULT_KEEP_ALIVE_TIME_MILLIS : duration;
            }
        };

        // @formatter:off
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(3000)
                .setConnectTimeout(3000)
                .setSocketTimeout(3000).build();

        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(poolingConnectionManager)
                .setKeepAliveStrategy(connectionKeepAliveStrategy)
                .setUserAgent("My secured Java App")
                .build();
        // @formatter:on

        return httpClient;
    }

    /**
     * Scheduled Methoden dürfen keine Parameter haben !
     *
     * @param poolingConnectionManager {@link PoolingHttpClientConnectionManager}
     * @return {@link Runnable}
     */
    @Bean
    public Runnable idleConnectionMonitor(final PoolingHttpClientConnectionManager poolingConnectionManager)
    {
        return new Runnable()
        {
            /**
             * @see java.lang.Runnable#run()
             */
            @Override
            @Scheduled(initialDelay = 10 * 1000, fixedDelay = 10 * 1000) // Alle 10 Sekunden
            // initialDelayString = #{ T(java.lang.Math).random() * 10 }
            // @Scheduled(cron = "0 0 * * * MON-FRI") // Jede Stunde von Montag - Freitag
            // @Scheduled(cron = "4,9,14,19,24,29,34,39,44,49,55,59 * * * *") // Alle 5 Minuten
            // @Scheduled(cron = "0 */15 * * * MON-FRI") // Alle 15 Minuten
            // @Async("executorService")
            public void run()
            {
                try
                {
                    if (poolingConnectionManager != null)
                    {
                        LOGGER.debug("idleConnectionMonitor - Closing expired and idle connections...");
                        poolingConnectionManager.closeExpiredConnections();
                        poolingConnectionManager.closeIdleConnections(CLOSE_IDLE_CONNECTION_WAIT_TIME_SECS, TimeUnit.SECONDS);
                    }
                    else
                    {
                        LOGGER.debug("idleConnectionMonitor - Http Client Connection manager is not initialised");
                    }
                }
                catch (Exception ex)
                {
                    LOGGER.error("idleConnectionMonitor - Exception occurred. msg={}, ex={}", ex.getMessage(), ex);
                }
            }
        };
    }

    /**
     * @param sslContext {@link SSLContext}
     * @return {@link PoolingHttpClientConnectionManager}
     * @throws Exception Falls was schief geht.
     */
    @Bean
    public PoolingHttpClientConnectionManager poolingConnectionManager(final SSLContext sslContext) throws Exception
    {
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());

        // @formatter:off
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory())
                .register("https", sslsf)
                .build();
        // @formatter:on

        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        poolingConnectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);

        return poolingConnectionManager;
    }

    /**
     * @return {@link SSLContext}
     * @throws Exception Falls was schief geht.
     */
    @Bean
    public SSLContext sslContext() throws Exception
    {
        PrivateKeyStrategy privateKeyStrategy = (aliases, socket) -> {
            System.out.println(aliases);
            return "server";
        };

        TrustStrategy trustStrategy = new TrustAllStrategy(); // (chain, authType) -> true;

        // @formatter:off
        SSLContext sslContext = SSLContextBuilder.create()
                .setKeyStoreType("PKCS12")
                .setSecureRandom(new SecureRandom())
                .loadKeyMaterial(ResourceUtils.getFile("classpath:server_keystore.p12"), "password".toCharArray(), "password".toCharArray(), privateKeyStrategy)
                .loadTrustMaterial(ResourceUtils.getFile("classpath:server_truststore.p12"), "password".toCharArray(), trustStrategy)
                .build();
        // @formatter:on

        return sslContext;
    }
}

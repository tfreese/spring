// Created: 16 Apr. 2025
package de.freese.spring.data.jpa;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.io.ManagedHttpClientConnection;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.io.HttpConnectionFactory;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.pool.PoolStats;
import org.apache.hc.core5.util.TimeValue;

/**
 * <a href=https://github.com/apache/httpcomponents-client/blob/5.4.x/httpclient5/src/test/java/org/apache/hc/client5/http/examples/ClientConfiguration.java>config</a>
 *
 * @author Thomas Freese
 */
public final class ApacheHttpClientConfigurer {
    public static CloseableHttpClient createCloseableHttpClient(final int maxRetries, final Duration retryInterval, final Consumer<Supplier<PoolStats>> poolStatsSupplierConsumer) {
        final HttpClientConnectionManager connectionManager = ApacheHttpClientConfigurer.createHttpClientConnectionManager(poolStatsSupplierConsumer);
        final RequestConfig requestConfig = ApacheHttpClientConfigurer.createRequestConfig();
        final ConnectionKeepAliveStrategy connectionKeepAliveStrategy = ApacheHttpClientConfigurer.createConnectionKeepAliveStrategy();
        final ConnectionReuseStrategy connectionReuseStrategy = ApacheHttpClientConfigurer.createConnectionReuseStrategy();
        final HttpRequestRetryStrategy httpRequestRetryStrategy = ApacheHttpClientConfigurer.createHttpRequestRetryStrategy(maxRetries, retryInterval);

        return HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .setKeepAliveStrategy(connectionKeepAliveStrategy)
                .setConnectionReuseStrategy(connectionReuseStrategy)
                .setRetryStrategy(httpRequestRetryStrategy)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofMinutes(15L))
                .setUserAgent("My Java App")
                .build();
    }

    public static ConnectionKeepAliveStrategy createConnectionKeepAliveStrategy() {
        return new DefaultConnectionKeepAliveStrategy() {
            @Override
            public TimeValue getKeepAliveDuration(final HttpResponse response, final HttpContext context) {
                final TimeValue duration = super.getKeepAliveDuration(response, context);

                return duration.getDuration() == -1L ? TimeValue.ofMinutes(2L) : duration;
            }
        };
    }

    public static ConnectionReuseStrategy createConnectionReuseStrategy() {
        return new DefaultConnectionReuseStrategy();
    }

    public static HttpClientConnectionManager createHttpClientConnectionManager(final Consumer<Supplier<PoolStats>> poolStatsSupplierConsumer) {
        final HttpConnectionFactory<ManagedHttpClientConnection> connectionSocketFactory = ApacheHttpClientConfigurer.createHttpConnectionFactory();

        // new NoopHostnameVerifier()
        // final TlsSocketStrategy tlsSocketStrategy = new DefaultClientTlsStrategy(SSLContext.getDefault(), HttpsURLConnection.getDefaultHostnameVerifier());

        final PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setConnectionFactory(connectionSocketFactory)
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setConnectTimeout(60L, TimeUnit.SECONDS)
                        .setSocketTimeout(60, TimeUnit.SECONDS)
                        .setTimeToLive(10L, TimeUnit.MINUTES)
                        .build())
                .setDefaultSocketConfig(SocketConfig.custom()
                        .setSoTimeout(120, TimeUnit.SECONDS)
                        .build())
                // .setTlsSocketStrategy(tlsSocketStrategy)
                .setDefaultTlsConfig(TlsConfig.custom()
                        .setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
                        .setHandshakeTimeout(1L, TimeUnit.MINUTES)
                        .setSupportedProtocols(TLS.V_1_3)
                        .build())
                // .setMaxConnPerRoute(20)
                .setMaxConnTotal(30)
                .setConnPoolPolicy(PoolReusePolicy.FIFO)
                .build();

        if (poolStatsSupplierConsumer != null) {
            poolStatsSupplierConsumer.accept(poolingHttpClientConnectionManager::getTotalStats);
        }

        return poolingHttpClientConnectionManager;
    }

    public static HttpConnectionFactory<ManagedHttpClientConnection> createHttpConnectionFactory() {
        final Http1Config http1Config = Http1Config.custom()
                .setChunkSizeHint(1_048_576)
                .setBufferSize(1_048_576)
                .build();

        final CharCodingConfig charCodingConfig = CharCodingConfig.custom()
                .setCharset(StandardCharsets.UTF_8)
                .build();

        return ManagedHttpClientConnectionFactory.builder()
                .http1Config(http1Config)
                .charCodingConfig(charCodingConfig)
                .build();
    }

    public static HttpRequestRetryStrategy createHttpRequestRetryStrategy(final int maxRetries, final Duration retryInterval) {
        return new DefaultHttpRequestRetryStrategy(maxRetries, TimeValue.of(retryInterval));
    }

    public static RequestConfig createRequestConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(3L, TimeUnit.SECONDS)
                .setResponseTimeout(3L, TimeUnit.SECONDS)
                .build();
    }

    private ApacheHttpClientConfigurer() {
        super();
    }
}

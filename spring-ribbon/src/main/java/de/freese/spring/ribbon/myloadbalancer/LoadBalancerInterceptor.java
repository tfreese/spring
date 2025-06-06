// Created: 21.03.2018
package de.freese.spring.ribbon.myloadbalancer;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;

/**
 * {@link ClientHttpRequestInterceptor} für den {@link LoadBalancer}.
 *
 * @author Thomas Freese
 */
public class LoadBalancerInterceptor implements ClientHttpRequestInterceptor {
    private static ClientHttpResponse intercept(final URI newUri, final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
        final HttpRequestWrapper requestWrapper = new HttpRequestWrapper(request) {
            @Override
            public URI getURI() {
                return newUri;
            }
        };

        return execution.execute(requestWrapper, body);
    }

    private final LoadBalancer loadBalancer;
    private final int retries;

    public LoadBalancerInterceptor(final LoadBalancer loadBalancer) {
        this(loadBalancer, 3);
    }

    /**
     * @param retries int; Anzahl der Versuche bei fehlerhaften Requests.
     */
    public LoadBalancerInterceptor(final LoadBalancer loadBalancer, final int retries) {
        super();

        this.loadBalancer = Objects.requireNonNull(loadBalancer, "loadBalancer required");

        if (retries <= 0) {
            throw new IllegalArgumentException("retries must be greater than 0");
        }

        this.retries = retries;
    }

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
        final URI originalUri = request.getURI();
        final String serviceName = originalUri.getHost();

        Exception lastException = null;

        for (int i = 0; i < retries; i++) {
            try {
                final URI newUri = loadBalancer.reconstructURI(serviceName, originalUri);

                return intercept(newUri, request, body, execution);
            }
            catch (Exception ex) {
                lastException = ex;
            }
        }

        if (lastException != null) {
            switch (lastException) {
                case IOException ex -> throw ex;
                case RuntimeException ex -> throw ex;
                default -> throw new IOException(lastException);
            }
        }

        return null;
    }
}

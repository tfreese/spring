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
public class LoadBalancerInterceptor implements ClientHttpRequestInterceptor
{
    /**
     *
     */
    private final LoadBalancer loadBalancer;

    /**
     *
     */
    private final int retries;

    /**
     * Erzeugt eine neue Instanz von {@link LoadBalancerInterceptor}.
     *
     * @param loadBalancer {@link LoadBalancer}
     */
    public LoadBalancerInterceptor(final LoadBalancer loadBalancer)
    {
        this(loadBalancer, 3);
    }

    /**
     * Erzeugt eine neue Instanz von {@link LoadBalancerInterceptor}.
     *
     * @param loadBalancer {@link LoadBalancer}
     * @param retries int; Anzahl der Versuche bei fehlerhaften Requests.
     */
    public LoadBalancerInterceptor(final LoadBalancer loadBalancer, final int retries)
    {
        super();

        this.loadBalancer = Objects.requireNonNull(loadBalancer, "loadBalancer required");

        if (retries <= 0)
        {
            throw new IllegalArgumentException("retries must be greater than 0");
        }

        this.retries = retries;
    }

    /**
     * @see org.springframework.http.client.ClientHttpRequestInterceptor#intercept(org.springframework.http.HttpRequest, byte[],
     *      org.springframework.http.client.ClientHttpRequestExecution)
     */
    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution)
            throws IOException
    {
        final URI originalUri = request.getURI();
        String serviceName = originalUri.getHost();

        Exception lastException = null;

        for (int i = 0; i < this.retries; i++)
        {
            try
            {
                URI newUri = this.loadBalancer.reconstructURI(serviceName, originalUri);

                ClientHttpResponse response = intercept(newUri, request, body, execution);

                return response;
            }
            catch (Exception ex)
            {
                lastException = ex;
            }
        }

        if (lastException != null)
        {
            if (lastException instanceof IOException)
            {
                throw (IOException) lastException;
            }
            else if (lastException instanceof RuntimeException)
            {
                throw (RuntimeException) lastException;
            }
            else
            {
                throw new IOException(lastException);
            }
        }

        return null;
    }

    /**
     * @param newUri {@link URI}
     * @param request {@link HttpRequest}
     * @param body byte[]
     * @param execution {@link ClientHttpRequestExecution}
     * @return {@link ClientHttpResponse}
     * @throws IOException Falls was schief geht.
     */
    private ClientHttpResponse intercept(final URI newUri, final HttpRequest request, final byte[] body,
            final ClientHttpRequestExecution execution) throws IOException
    {
        HttpRequestWrapper requestWrapper = new HttpRequestWrapper(request)
        {
            /**
             * @see org.springframework.http.client.support.HttpRequestWrapper#getURI()
             */
            @Override
            public URI getURI()
            {
                return newUri;
            }
        };

        return execution.execute(requestWrapper, body);
    }
}

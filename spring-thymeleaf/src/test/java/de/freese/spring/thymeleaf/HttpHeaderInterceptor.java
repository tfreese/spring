// Created: 16.09.2020
package de.freese.spring.thymeleaf;

import java.io.IOException;
import java.util.Objects;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * @author Thomas Freese
 */
public class HttpHeaderInterceptor implements ClientHttpRequestInterceptor
{
    /**
     *
     */
    private final String name;

    /**
     *
     */
    private final String value;

    /**
     * Erstellt ein neues {@link HttpHeaderInterceptor} Object.
     *
     * @param name String
     * @param value String
     */
    public HttpHeaderInterceptor(final String name, final String value)
    {
        super();

        this.name = Objects.requireNonNull(name, "name required");
        this.value = Objects.requireNonNull(value, "value required");
    }

    /**
     * @see org.springframework.http.client.ClientHttpRequestInterceptor#intercept(org.springframework.http.HttpRequest, byte[],
     *      org.springframework.http.client.ClientHttpRequestExecution)
     */
    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException
    {
        request.getHeaders().add(this.name, this.value);

        return execution.execute(request, body);
    }
}

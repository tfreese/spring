// Created: 13.05.2026
package de.freese.spring.data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.BearerToken;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Preemptive Authentication sends always send Auth-Data.<br>
 * Usage of CredentialsProvider: 1st Request is send without Auth-Data, if 401 the 2nd Request is sent with Auth-Data (1 extra Round-Trip).<br>
 * <code>httpClientBuilder.addRequestInterceptorFirst(new PreemptiveAuthenticationRequestInterceptor(credentialsProvider))</code>
 *
 * @author Thomas Freese
 */
public final class PreemptiveAuthenticationRequestInterceptor implements HttpRequestInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreemptiveAuthenticationRequestInterceptor.class);

    private final CredentialsProvider credentialsProvider;

    public PreemptiveAuthenticationRequestInterceptor(final CredentialsProvider credentialsProvider) {
        super();

        this.credentialsProvider = Objects.requireNonNull(credentialsProvider, "credentialsProvider required");
    }

    @Override
    public void process(final HttpRequest request, final EntityDetails entity, final HttpContext context) throws HttpException, IOException {
        if (request.containsHeader(HttpHeaders.AUTHORIZATION)) {
            LOGGER.debug("AUTHORIZATION Header is already set: return");
            return;
        }

        final HttpClientContext clientContext = HttpClientContext.cast(context);
        final HttpHost httpHost = clientContext.getHttpRoute() != null ? clientContext.getHttpRoute().getTargetHost() : null;

        if (httpHost == null) {
            LOGGER.debug("No HttpHost found in HttpContext: return");
            return;
        }

        // If set in HttpClientContext.
        // CredentialsProvider credentialsProvider = clientContext.getCredentialsProvider();

        final Credentials credentials = getCredentials(context, httpHost);

        if (credentials == null) {
            LOGGER.debug("No credentials found for: {}", httpHost);
            return;
        }

        final String headerValue = toHeaderValue(credentials);

        if (headerValue == null) {
            // Unsupported credential type.
            return;
        }

        request.setHeader(HttpHeaders.AUTHORIZATION, headerValue);
    }

    private Credentials getCredentials(final HttpContext context, final HttpHost httpHost) {
        return credentialsProvider.getCredentials(
                new AuthScope(httpHost.getSchemeName(), httpHost.getHostName(), httpHost.getPort(), null, null),
                context
        );
    }

    private String toHeaderValue(final Credentials credentials) {
        if (credentials instanceof UsernamePasswordCredentials upc) {
            return toHeaderValue(upc);
        }
        else if (credentials instanceof BearerToken bt) {
            return toHeaderValue(bt);
        }

        LOGGER.warn("Unsupported credential type: {}", credentials.getClass());

        return null;
    }

    private String toHeaderValue(final UsernamePasswordCredentials credentials) {
        final String user = credentials.getUserPrincipal().getName();
        final char[] pass = credentials.getUserPassword();
        final String raw = user + ":" + new String(pass);

        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.ISO_8859_1));
    }

    private String toHeaderValue(final BearerToken bearerToken) {
        return "Bearer " + bearerToken.getToken();
    }
}
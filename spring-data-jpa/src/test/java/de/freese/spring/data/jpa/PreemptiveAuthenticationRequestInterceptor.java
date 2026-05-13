// Created: 13.05.2026
package de.freese.spring.data.jpa;

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

/**
 * Preemptive Authentication sends always send Auth-Data.<br>
 * Usage of CredentialsProvider: 1st Request sends without Auth-Data, if 401 the 2nd Request is sent with Auth-Data (1 extra Round-Trip).<br>
 * <code>httpClientBuilder.addRequestInterceptorFirst(new PreemptiveAuthenticationRequestInterceptor(credentialsProvider))</code>
 *
 * @author Thomas Freese
 */
public final class PreemptiveAuthenticationRequestInterceptor implements HttpRequestInterceptor {
    private final CredentialsProvider credentialsProvider;

    public PreemptiveAuthenticationRequestInterceptor(final CredentialsProvider credentialsProvider) {
        super();

        this.credentialsProvider = Objects.requireNonNull(credentialsProvider, "credentialsProvider required");
    }

    @Override
    public void process(final HttpRequest request, final EntityDetails entity, final HttpContext context) throws HttpException, IOException {
        if (request.containsHeader(HttpHeaders.AUTHORIZATION)) {
            return;
        }

        final HttpClientContext clientContext = HttpClientContext.cast(context);
        final HttpHost httpHost = clientContext.getHttpRoute() != null
                ? clientContext.getHttpRoute().getTargetHost()
                : null;

        if (httpHost == null) {
            return;
        }

        // If set in HttpClientContext.
        // CredentialsProvider credentialsProvider = clientContext.getCredentialsProvider();

        if ("host1".equalsIgnoreCase(httpHost.getHostName())) {
            final Credentials credentials = getCredentials(context, httpHost);

            if (!(credentials instanceof final UsernamePasswordCredentials up)) {
                throw new IllegalStateException("No UsernamePasswordCredentials configured for host " + httpHost);
            }

            request.setHeader(HttpHeaders.AUTHORIZATION, toHeaderValue(up));
        }
        else if ("host2".equalsIgnoreCase(httpHost.getHostName())) {
            final Credentials credentials = getCredentials(context, httpHost);

            if (!(credentials instanceof final BearerToken bt)) {
                throw new IllegalStateException("No BearerToken configured for host " + httpHost);
            }

            request.setHeader(HttpHeaders.AUTHORIZATION, toHeaderValue(bt));
        }
    }

    private Credentials getCredentials(final HttpContext context, final HttpHost httpHost) {
        return credentialsProvider.getCredentials(
                new AuthScope(httpHost.getSchemeName(), httpHost.getHostName(), httpHost.getPort(), null, null),
                context
        );
    }

    private String toHeaderValue(final UsernamePasswordCredentials credentials) {
        final String user = credentials.getUserPrincipal().getName();
        final char[] pass = credentials.getUserPassword();
        final String raw = user + ":" + new String(pass);
        final String b64 = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.ISO_8859_1));

        return "Basic " + b64;
    }

    private String toHeaderValue(final BearerToken bearerToken) {
        return "Bearer " + bearerToken.getToken();
    }

}
// Created: 14.05.2026
package de.freese.spring.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.Principal;

import org.apache.hc.client5.http.auth.BearerToken;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.impl.auth.CredentialsProviderBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.StatusLine;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Freese
 */
class TestApacheHttpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestApacheHttpClient.class);

    private final HttpHost HOST = new HttpHost("https", "httpbin.org", 443);

    /**
     * Usage of CredentialsProvider: 1st Request is send without Auth-Data, if 401 the 2nd Request is sent with Auth-Data (1 extra Round-Trip).
     */
    @Test
    void testAuthBasic() throws Exception {
        final CredentialsProvider credentialsProvider = CredentialsProviderBuilder.create()
                .add(HOST, "user", "passwd".toCharArray())
                .build();

        try (final CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credentialsProvider)
                .build()) {

            final ClassicHttpRequest httpRequest = ClassicRequestBuilder
                    .get(HOST.toURI() + "/basic-auth/user/passwd")
                    .setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType())
                    .build();

            LOGGER.info("Executing request {} {}", httpRequest.getMethod(), httpRequest.getUri());

            final int httpStatus = httpclient.execute(httpRequest, response -> {
                LOGGER.info("{} -> {}", httpRequest, new StatusLine(response));

                EntityUtils.consume(response.getEntity());

                return response.getCode();
            });

            assertEquals(HttpStatus.SC_OK, httpStatus);
        }
    }

    /**
     * Usage of CredentialsProvider: 1st Request is send without Auth-Data, if 401 the 2nd Request is sent with Auth-Data (1 extra Round-Trip)
     */
    @Test
    void testAuthBearer() throws Exception {
        final CredentialsProvider credentialsProvider = CredentialsProviderBuilder.create()
                .add(HOST, new BearerToken("TOKEN"))
                .build();

        try (final CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credentialsProvider)
                .build()) {

            final ClassicHttpRequest httpRequest = ClassicRequestBuilder
                    .get(HOST.toURI() + "/bearer")
                    .setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType())
                    .build();

            LOGGER.info("Executing request {} {}", httpRequest.getMethod(), httpRequest.getUri());

            final int httpStatus = httpclient.execute(httpRequest, response -> {
                LOGGER.info("{} -> {}", httpRequest, new StatusLine(response));

                EntityUtils.consume(response.getEntity());

                return response.getCode();
            });

            assertEquals(HttpStatus.SC_OK, httpStatus);
        }
    }

    /**
     * Request is send only once.
     */
    @Test
    void testPreemptiveAuthBasic() throws Exception {
        final CredentialsProvider credentialsProvider = CredentialsProviderBuilder.create()
                .add(HOST, "user", "passwd".toCharArray())
                .add(new HttpHost("http", "httpbin.org", 80), new BearerToken("TOKEN")) // Won't work.
                .build();

        try (final CloseableHttpClient httpclient = HttpClients.custom()
                .addRequestInterceptorFirst(new PreemptiveAuthenticationRequestInterceptor(credentialsProvider))
                .build()) {

            final ClassicHttpRequest httpRequest = ClassicRequestBuilder
                    .get(HOST.toURI() + "/basic-auth/user/passwd")
                    .setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType())
                    .build();

            LOGGER.info("Executing request {} {}", httpRequest.getMethod(), httpRequest.getUri());

            final int httpStatus = httpclient.execute(httpRequest, response -> {
                LOGGER.info("{} -> {}", httpRequest, new StatusLine(response));

                EntityUtils.consume(response.getEntity());

                return response.getCode();
            });

            assertEquals(HttpStatus.SC_OK, httpStatus);
        }
    }

    /**
     * Request is send only once.
     */
    @Test
    void testPreemptiveAuthBearer() throws Exception {
        final CredentialsProvider credentialsProvider = CredentialsProviderBuilder.create()
                .add(HOST, new BearerToken("TOKEN"))
                .add(new HttpHost("http", "httpbin.org", 80), "user", "passwd".toCharArray()) // Won't work.
                .build();

        try (final CloseableHttpClient httpclient = HttpClients.custom()
                .addRequestInterceptorFirst(new PreemptiveAuthenticationRequestInterceptor(credentialsProvider))
                .build()) {

            final ClassicHttpRequest httpRequest = ClassicRequestBuilder
                    .get(HOST.toURI() + "/bearer")
                    .setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType())
                    .build();

            LOGGER.info("Executing request {} {}", httpRequest.getMethod(), httpRequest.getUri());

            final int httpStatus = httpclient.execute(httpRequest, response -> {
                LOGGER.info("{} -> {}", httpRequest, new StatusLine(response));

                EntityUtils.consume(response.getEntity());

                return response.getCode();
            });

            assertEquals(HttpStatus.SC_OK, httpStatus);
        }
    }

    @Test
    void testUnsupportedCredentialType() throws Exception {
        final Credentials unsupportedCredentialType = new Credentials() {
            @Override
            public char[] getPassword() {
                return new char[0];
            }

            @Override
            public Principal getUserPrincipal() {
                return null;
            }
        };

        final CredentialsProvider credentialsProvider = CredentialsProviderBuilder.create()
                .add(HOST, unsupportedCredentialType)
                .build();

        try (final CloseableHttpClient httpclient = HttpClients.custom()
                .addRequestInterceptorFirst(new PreemptiveAuthenticationRequestInterceptor(credentialsProvider))
                .build()) {

            final ClassicHttpRequest httpRequest = ClassicRequestBuilder
                    .get(HOST.toURI() + "/bearer")
                    .setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType())
                    .build();

            LOGGER.info("Executing request {} {}", httpRequest.getMethod(), httpRequest.getUri());

            final int httpStatus = httpclient.execute(httpRequest, response -> {
                LOGGER.info("{} -> {}", httpRequest, new StatusLine(response));

                EntityUtils.consume(response.getEntity());

                return response.getCode();
            });

            assertEquals(HttpStatus.SC_UNAUTHORIZED, httpStatus);
        }
    }
}

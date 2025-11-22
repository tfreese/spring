// Created: 11.10.2025
package de.freese.spring.data.jpa.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import jakarta.annotation.Resource;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.HttpEntities;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.junit.jupiter.api.Test;

import de.freese.spring.data.jpa.domain.Todo;

/**
 * @author Thomas Freese
 */
class TestApacheHttpClient extends AbstractClientTest {
    @Resource
    private CloseableHttpClient httpClient;

    // @AfterAll
    // static void afterAll() {
    //     httpClient.close(CloseMode.GRACEFUL);
    //     httpClient = null;
    // }
    //
    // @BeforeAll
    // static void beforeAll() {
    //     httpClient = ApacheHttpClientConfigurer.createCloseableHttpClient(3, Duration.ofSeconds(3), null);
    // }

    @Test
    @Override
    void testCreateTodo() {
        final Todo todo = creaTodo();

        try {
            final ClassicHttpRequest httpRequest = ClassicRequestBuilder
                    .post(getUrl() + "/api/todo")
                    .setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType())
                    .setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                    .setEntity(HttpEntities.create(getJsonMapper().writeValueAsString(todo), StandardCharsets.UTF_8))
                    .build();

            httpClient.execute(httpRequest, response -> {
                assertEquals(HttpStatus.SC_CREATED, response.getCode());
                assertEquals("", response.getReasonPhrase());

                final String body = EntityUtils.toString(response.getEntity());

                getLogger().info(body);

                assertNotNull(body);
                assertTrue(body.startsWith("{"));
                assertTrue(body.strip().endsWith("}"));

                return null;
            });
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Test
    @Override
    void testGetAllTodosJson() {
        testCreateTodo();

        final ClassicHttpRequest httpRequest = ClassicRequestBuilder
                .get(getUrl() + "/api/todo")
                .setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType())
                .build();

        try {
            httpClient.execute(httpRequest, response -> {
                assertEquals(HttpStatus.SC_OK, response.getCode());
                assertEquals("", response.getReasonPhrase());

                final String body = EntityUtils.toString(response.getEntity());

                getLogger().info(body);

                assertNotNull(body);
                assertTrue(body.startsWith("[ {"));
                assertTrue(body.strip().endsWith("} ]"));

                return null;
            });
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Test
    @Override
    void testGetAllTodosXml() {
        testCreateTodo();

        final ClassicHttpRequest httpRequest = ClassicRequestBuilder
                .get(getUrl() + "/api/todo")
                .setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_XML.getMimeType())
                .build();

        try {
            httpClient.execute(httpRequest, response -> {
                assertEquals(HttpStatus.SC_OK, response.getCode());
                assertEquals("", response.getReasonPhrase());

                final String body = EntityUtils.toString(response.getEntity());

                getLogger().info(body);

                assertNotNull(body);
                assertTrue(body.startsWith("<"));
                assertTrue(body.strip().endsWith(">"));

                return null;
            });
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Test
    @Override
    void testNotFound() {
        final ClassicHttpRequest httpRequest = ClassicRequestBuilder
                .get(getUrl() + "/api/todo/" + UUID.randomUUID())
                .setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType())
                .build();

        try {
            httpClient.execute(httpRequest, response -> {
                assertEquals(HttpStatus.SC_NOT_FOUND, response.getCode());
                assertEquals("", response.getReasonPhrase());
                assertTrue(EntityUtils.toString(response.getEntity()).contains("Todo not found by ID:"));

                return null;
            });
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Test
    @Override
    void testStream() {
        final String url = getUrl() + "/api/todo/" + UUID.randomUUID() + "/stream";

        try (HttpEntity httpEntity = HttpEntities.create(outputStream -> {
            outputStream.write("From Client: Hello World".getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }, ContentType.APPLICATION_OCTET_STREAM)) {

            ClassicHttpRequest httpRequest = ClassicRequestBuilder
                    .post(url)
                    .setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM.getMimeType())
                    .setEntity(httpEntity)
                    .build();

            httpClient.execute(httpRequest, response -> {
                assertEquals(HttpStatus.SC_OK, response.getCode());
                assertEquals("", response.getReasonPhrase());
                assertEquals("", EntityUtils.toString(response.getEntity()));

                return null;
            });

            httpRequest = ClassicRequestBuilder
                    .get(url)
                    .setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_OCTET_STREAM.getMimeType())
                    .build();

            // BasicHttpClientResponseHandler
            httpClient.execute(httpRequest, response -> {
                assertEquals(HttpStatus.SC_OK, response.getCode());
                assertEquals("", response.getReasonPhrase());
                assertEquals("From Server: Hello World", EntityUtils.toString(response.getEntity()));

                // Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                // BufferedReader bufferedReader = new BufferedReader(reader)
                // try (InputStream inputStream = response.getEntity().getContent()) {
                //     // final String message = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
                //     final String message = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                //     assertEquals("From Server: Hello World", message);
                //
                //     LOGGER.info(message);
                // }

                return null;
            });
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}

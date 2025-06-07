// Created:07.09.2018
package de.freese.spring.thymeleaf.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;

import jakarta.annotation.Resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import de.freese.spring.thymeleaf.ThymeleafApplication;
import de.freese.spring.thymeleaf.model.Person;

/**
 * @author Thomas Freese
 */
@ActiveProfiles("test")
class TestRestWithJreHttpClient extends AbstractRestTestCase {
    @Resource
    private ExecutorService executorService;
    private String rootUri;

    @BeforeEach
    void beforeTest() {
        rootUri = ThymeleafApplication.getRootUri(getEnvironment());
    }

    @Override
    @Test
    void testHealthEndpoint() throws Exception {
        try (HttpClient httpClient = createClientBuilder().build()) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(rootUri + "/actuator/info"))
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .GET()
                    .build();

            final HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            assertEquals(MediaType.APPLICATION_JSON_VALUE, response.headers().firstValue("Content-Type").orElse(null));
            assertEquals(HttpStatus.OK.value(), response.statusCode());

            // final Object status = JsonPath.parse(response.body()).read("$.status");
            // assertEquals("UP", status);
        }
    }

    @Override
    @Test
    void testPost() throws Exception {
        try (HttpClient httpClient = createClientBuilder("admin", "pw").build()) {
            // POST
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(rootUri + "/rest/person/personAdd"))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .POST(BodyPublishers.ofString("{\"firstName\":\"Thomas\",\"lastName\":\"Freese\"}", StandardCharsets.UTF_8))
                    .build();

            final HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            assertEquals(HttpStatus.OK.value(), response.statusCode());
        }

        try (HttpClient httpClient = createClientBuilder("user", "pw").build()) {
            // GET
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(rootUri + "/rest/person/personList"))
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .GET()
                    .build();

            final HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            final List<Person> persons = getObjectMapper().readValue(response.body(), new TypeReference<>() {
            });

            assertNotNull(persons);
            assertTrue(persons.size() >= 2);
        }
    }

    @Override
    @Test
    void testPostWithWrongRole() throws Exception {
        try (HttpClient httpClient = createClientBuilder("user", "pw").build()) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(rootUri + "/rest/person/personAdd"))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .POST(BodyPublishers.ofString("{\"firstName\":\"Thomas\",\"lastName\":\"Freese\"}", StandardCharsets.UTF_8))
                    .build();

            final HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            assertEquals(HttpStatus.FORBIDDEN.value(), response.statusCode());
        }
    }

    @Override
    @Test
    void testUserWithLoginJSON() throws Exception {
        try (HttpClient httpClient = createClientBuilder("user", "pw").build()) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(rootUri + "/rest/person/personList"))
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .GET()
                    .build();

            final HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            final List<Person> persons = getObjectMapper().readValue(response.body(), new TypeReference<>() {
            });

            assertNotNull(persons);
            assertTrue(persons.size() >= 2);
        }
    }

    @Override
    @Test
    void testUserWithLoginXML() throws Exception {
        final ObjectMapper objectMapperXML = getObjectMapperBuilder().createXmlMapper(true).build();

        try (HttpClient httpClient = createClientBuilder("user", "pw").build()) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(rootUri + "/rest/person/personList"))
                    .header("Accept", MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8")
                    .GET()
                    .build();

            final HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            final List<Person> persons = objectMapperXML.readValue(response.body(), new TypeReference<>() {
            });

            assertNotNull(persons);
            assertTrue(persons.size() >= 2);
        }
    }

    @Override
    @Test
    void testUserWithPreAuthJSON() throws Exception {
        try (HttpClient httpClient = createClientBuilder().build()) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(rootUri + "/rest/person/personList"))
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .header("my-token", "user")
                    .GET()
                    .build();

            final HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            final List<Person> persons = getObjectMapper().readValue(response.body(), new TypeReference<>() {
            });

            assertNotNull(persons);
            assertTrue(persons.size() >= 2);
        }
    }

    @Override
    @Test
    void testUserWithPreAuthXML() throws Exception {
        final ObjectMapper objectMapperXML = getObjectMapperBuilder().createXmlMapper(true).build();

        try (HttpClient httpClient = createClientBuilder().build()) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(rootUri + "/rest/person/personList"))
                    .header("Accept", MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8")
                    .header("my-token", "user")
                    .GET()
                    .build();

            final HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            final List<Person> persons = objectMapperXML.readValue(response.body(), new TypeReference<>() {
            });

            assertNotNull(persons);
            assertTrue(persons.size() >= 2);
        }
    }

    @Override
    @Test
    void testUserWithWrongPass() {
        final IOException ioException = Assertions.assertThrows(IOException.class, () -> {
            try (HttpClient httpClient = createClientBuilder("user", "pass").build()) {
                final HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(rootUri + "/rest/person/personList"))
                        .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                        .GET()
                        .build();

                try {
                    // HttpResponse<String> response =
                    httpClient.send(request, BodyHandlers.ofString());
                    // Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode());
                    fail("sollte nicht erfolgreich sein");
                }
                catch (Exception ex) {
                    assertEquals("too many authentication attempts. Limit: 3", ex.getMessage());
                    throw ex;
                }
            }
        });

        assertNotNull(ioException);
    }

    @Override
    @Test
    void testUserWithWrongRole() throws Exception {
        try (HttpClient httpClient = createClientBuilder("invalid", "pw").build()) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(rootUri + "/rest/person/personList"))
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .GET()
                    .build();

            final HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            assertEquals(HttpStatus.FORBIDDEN.value(), response.statusCode());
        }
    }

    /**
     * <pre>{@code
     * (expected = IOException.class)
     * }</pre>
     */
    @Override
    @Test
    void testUserWithoutLogin() throws Exception {
        try (HttpClient httpClient = createClientBuilder().build()) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(rootUri + "/rest/person/personList"))
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .GET()
                    .build();

            try {
                final HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
                assertEquals(HttpStatus.UNAUTHORIZED.value(), response.statusCode());
                // Assertions.fail("sollte nicht erfolgreich sein");
            }
            catch (Exception ex) {
                assertEquals("No authenticator set", ex.getMessage());
                throw ex;
            }
        }
    }

    private HttpClient.Builder createClientBuilder() {
        return HttpClient.newBuilder()
                .version(Version.HTTP_2)
                .executor(executorService);
    }

    private HttpClient.Builder createClientBuilder(final String user, final String password) {
        final Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password.toCharArray());
            }
        };

        Authenticator.setDefault(authenticator);

        return createClientBuilder()
                .authenticator(authenticator)
                .version(Version.HTTP_1_1) // Mit HTTP2 kommen Fehler wie "/127.0.0.1:39304: GOAWAY received"
                ;
    }
}

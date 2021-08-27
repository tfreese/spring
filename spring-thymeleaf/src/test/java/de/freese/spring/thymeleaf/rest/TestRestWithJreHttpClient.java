// Created:07.09.2018
package de.freese.spring.thymeleaf.rest;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.annotation.Resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import de.freese.spring.thymeleaf.ThymeleafApplication;
import de.freese.spring.thymeleaf.model.Person;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = ThymeleafApplication.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TestRestWithJreHttpClient extends AbstractRestTestCase
{
    /**
    *
    */
    @Resource
    private Environment environment;

    /**
    *
    */
    @Resource
    private ExecutorService executorService;

    /**
    *
    */
    @LocalServerPort
    private int localServerPort;

    /**
     * Default für JSON.
     */
    @Resource
    private ObjectMapper objectMapper;

    /**
     * Für XML-Mapping
     */
    @Resource
    private Jackson2ObjectMapperBuilder objectMapperBuilder;

    /**
    *
    */
    private String rootUri;

    /**
     * @throws Exception Falls was schief geht.
     */
    @BeforeEach
    void beforeTest() throws Exception
    {
        this.rootUri = ThymeleafApplication.getRootUri(this.environment);
    }

    /**
     * @return {@link Builder}
     */
    private HttpClient.Builder createClientBuilder()
    {
        // @formatter:off
        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(Version.HTTP_2)
                .executor(this.executorService)
                ;
        // @formatter:on

        return builder;
    }

    /**
     * @param user String
     * @param password String
     *
     * @return {@link Builder}
     */
    private HttpClient.Builder createClientBuilder(final String user, final String password)
    {
        Authenticator authenticator = new Authenticator()
        {
            /**
             * @see java.net.Authenticator#getPasswordAuthentication()
             */
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(user, password.toCharArray());
            }
        };

        Authenticator.setDefault(authenticator);

        // @formatter:off
        HttpClient.Builder builder = createClientBuilder()
                .authenticator(authenticator)
                .version(Version.HTTP_1_1) // Mit HTTP2 kommen Fehler wie "/127.0.0.1:39304: GOAWAY received"
                ;
        // @formatter:on

        return builder;
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test000HealthEndpoint()
     */
    @Override
    @Test
    void test000HealthEndpoint() throws Exception
    {
        HttpClient httpClient = createClientBuilder().build();

        // @formatter:off
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.rootUri + "/actuator/health"))
                .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                .GET()
                .build();
        // @formatter:on

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE, response.headers().firstValue("Content-Type").get());

        Object status = JsonPath.parse(response.body()).read("$.status");
        // assertEquals("UP", status);
        assertNotNull(status);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test010UserWithoutLogin()
     */
    @Override
    @Test// (expected = IOException.class)
    void test010UserWithoutLogin() throws Exception
    {
        HttpClient httpClient = createClientBuilder().build();

        // @formatter:off
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.rootUri + "/rest/person/personList"))
                .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                .GET()
                .build();
        // @formatter:on

        try
        {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.statusCode());
            // Assertions.fail("sollte nicht erfolgreich sein");
        }
        catch (Exception ex)
        {
            Assertions.assertEquals("No authenticator set", ex.getMessage());
            throw ex;
        }
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test011UserWithWrongPass()
     */
    @Override
    @Test
    void test011UserWithWrongPass() throws Exception
    {
        Assertions.assertThrows(IOException.class, () -> {
            HttpClient httpClient = createClientBuilder("user", "pass").build();

        // @formatter:off
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.rootUri + "/rest/person/personList"))
                .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                .GET()
                .build()
                ;
        // @formatter:on

            try
            {
                // HttpResponse<String> response =
                httpClient.send(request, BodyHandlers.ofString());
                // Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode());
                Assertions.fail("sollte nicht erfolgreich sein");
            }
            catch (Exception ex)
            {
                Assertions.assertEquals("too many authentication attempts. Limit: 3", ex.getMessage());
                throw ex;
            }
        });
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test020UserWithWrongRole()
     */
    @Override
    @Test
    void test020UserWithWrongRole() throws Exception
    {
        HttpClient httpClient = createClientBuilder("invalid", "pw").build();

        // @formatter:off
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.rootUri + "/rest/person/personList"))
                .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                .GET()
                .build()
                ;
        // @formatter:on

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.statusCode());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test030UserWithLoginJSON()
     */
    @Override
    @Test
    void test030UserWithLoginJSON() throws Exception
    {
        HttpClient httpClient = createClientBuilder("user", "pw").build();

        // @formatter:off
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.rootUri + "/rest/person/personList"))
                .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                .GET()
                .build()
                ;
        // @formatter:on

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        List<Person> persons = this.objectMapper.readValue(response.body(), new TypeReference<List<Person>>()
        {
        });

        Assertions.assertNotNull(persons);
        Assertions.assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test031UserWithLoginXML()
     */
    @Override
    @Test
    void test031UserWithLoginXML() throws Exception
    {
        ObjectMapper objectMapperXML = this.objectMapperBuilder.createXmlMapper(true).build();
        HttpClient httpClient = createClientBuilder("user", "pw").build();

        // @formatter:off
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.rootUri + "/rest/person/personList"))
                .header("Accept", MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8")
                .GET()
                .build()
                ;
        // @formatter:on

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        List<Person> persons = objectMapperXML.readValue(response.body(), new TypeReference<List<Person>>()
        {
        });

        Assertions.assertNotNull(persons);
        Assertions.assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test040PostWithWrongRole()
     */
    @Override
    @Test
    void test040PostWithWrongRole() throws Exception
    {
        HttpClient httpClient = createClientBuilder("user", "pw").build();

        // @formatter:off
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.rootUri + "/rest/person/personAdd"))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(BodyPublishers.ofString("{\"firstName\":\"Thomas\",\"lastName\":\"Freese\"}", StandardCharsets.UTF_8))
                .build()
                ;
        // @formatter:on

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.statusCode());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test041Post()
     */
    @Override
    @Test
    void test041Post() throws Exception
    {
        // POST
        HttpClient httpClient = createClientBuilder("admin", "pw").build();

        // @formatter:off
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.rootUri + "/rest/person/personAdd"))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(BodyPublishers.ofString("{\"firstName\":\"Thomas\",\"lastName\":\"Freese\"}", StandardCharsets.UTF_8))
                .build()
                ;
        // @formatter:on

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        Assertions.assertEquals(HttpStatus.OK.value(), response.statusCode());

        // GET
        httpClient = createClientBuilder("user", "pw").build();

        // @formatter:off
        request = HttpRequest.newBuilder()
                .uri(URI.create(this.rootUri + "/rest/person/personList"))
                .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                .GET()
                .build()
                ;
        // @formatter:on

        response = httpClient.send(request, BodyHandlers.ofString());

        List<Person> persons = this.objectMapper.readValue(response.body(), new TypeReference<List<Person>>()
        {
        });

        Assertions.assertNotNull(persons);
        Assertions.assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test050UserWithPreAuthJSON()
     */
    @Override
    @Test
    void test050UserWithPreAuthJSON() throws Exception
    {
        HttpClient httpClient = createClientBuilder().build();

        // @formatter:off
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.rootUri + "/rest/person/personList"))
                .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                .header("my-token", "user")
                .GET()
                .build()
                ;
        // @formatter:on

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        List<Person> persons = this.objectMapper.readValue(response.body(), new TypeReference<List<Person>>()
        {
        });

        Assertions.assertNotNull(persons);
        Assertions.assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test051UserWithPreAuthXML()
     */
    @Override
    @Test
    void test051UserWithPreAuthXML() throws Exception
    {
        ObjectMapper objectMapperXML = this.objectMapperBuilder.createXmlMapper(true).build();
        HttpClient httpClient = createClientBuilder().build();

        // @formatter:off
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.rootUri + "/rest/person/personList"))
                .header("Accept", MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8")
                .header("my-token", "user")
                .GET()
                .build()
                ;
        // @formatter:on

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        List<Person> persons = objectMapperXML.readValue(response.body(), new TypeReference<List<Person>>()
        {
        });

        Assertions.assertNotNull(persons);
        Assertions.assertTrue(persons.size() >= 2);
    }
}

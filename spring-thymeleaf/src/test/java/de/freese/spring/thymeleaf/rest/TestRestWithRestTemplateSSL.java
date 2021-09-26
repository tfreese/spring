// Created: 07.09.2018
package de.freese.spring.thymeleaf.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.apache.http.client.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.JsonPath;

import de.freese.spring.thymeleaf.HttpHeaderInterceptor;
import de.freese.spring.thymeleaf.ThymeleafApplication;
import de.freese.spring.thymeleaf.exception.ApiError;
import de.freese.spring.thymeleaf.model.Person;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = ThymeleafApplication.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
@AutoConfigureMockMvc
@ActiveProfiles(
{
        "test", "with-ssl"
})
class TestRestWithRestTemplateSSL extends AbstractRestTestCase
{
    /**
     * @author Thomas Freese
     */
    private static class NoOpResponseErrorHandler extends DefaultResponseErrorHandler
    {
        /**
         * @see org.springframework.web.client.DefaultResponseErrorHandler#handleError(org.springframework.http.client.ClientHttpResponse)
         */
        @Override
        public void handleError(final ClientHttpResponse response) throws IOException
        {
            // Das Auslesen des Responses ist nur einmal möglich !
            // Das für bei den Tests zu Fehlern.

            // RestClientResponseException exception =
            // new RestClientResponseException("Server Error: [" + response.getRawStatusCode() + "]" + " " + response.getStatusText(),
            // response.getRawStatusCode(), response.getStatusText(), response.getHeaders(), getResponseBody(response), getCharset(response));
            //
            // System.err.println(exception);
            // // exception.printStackTrace();
            //
            // try
            // {
            // ApiError apiError = TestRestApi.this.objectMapper.readValue(exception.getResponseBodyAsByteArray(), ApiError.class);
            // // exception.setStackTrace(apiError.getStackTrace());
            // System.err.println(apiError);
            // }
            // catch (Exception ex)
            // {
            // }
        }
    }

    /**
    *
    */
    @Resource
    private Environment environment;
    /**
     *
     */
    @Resource
    private HttpClient httpClient;
    /**
    *
    */
    @LocalServerPort
    private int localServerPort;
    /**
     *
     */
    @Resource
    private RestTemplateBuilder restTemplateBuilder;

    /**
     * @throws Exception Falls was schief geht.
     */
    @BeforeEach
    void beforeTest() throws Exception
    {
        // String rootUri = "http://localhost:" + this.localServerPort;
        String rootUri = ThymeleafApplication.getRootUri(this.environment);

        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(this.httpClient);

        // @formatter:off
        this.restTemplateBuilder = this.restTemplateBuilder
                .rootUri(rootUri)
                .errorHandler(new NoOpResponseErrorHandler())
                .requestFactory(() -> httpRequestFactory);
        // @formatter:on
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test000HealthEndpoint()
     */
    @Override
    @Test
    void test000HealthEndpoint() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/actuator/health", String.class);

        Assertions.assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());

        String status = JsonPath.parse(responseEntity.getBody()).read("$.status");
        Assertions.assertEquals("UP", status);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test010UserWithoutLogin()
     */
    @Override
    @Test
    void test010UserWithoutLogin() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .build();
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test011UserWithWrongPass()
     */
    @Override
    @Test
    void test011UserWithWrongPass() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pass", StandardCharsets.UTF_8))
                .build();
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test020UserWithWrongRole()
     */
    @Override
    @Test
    void test020UserWithWrongRole() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("invalid", "pw", StandardCharsets.UTF_8))
                .build();
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);

        Assertions.assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test030UserWithLoginJSON()
     */
    @Override
    @Test
    void test030UserWithLoginJSON() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();
        // @formatter:on

        // ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);
        // persons = this.objectMapper.readValue(responseEntity.getBody(), new TypeReference<List<Person>>()
        // {
        // });

        // Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        // persons = Arrays.asList(personArray);

        ResponseEntity<List<Person>> responseEntity =
                restTemplate.exchange("/rest/person/personList", HttpMethod.GET, null, new ParameterizedTypeReference<List<Person>>()
                {
                });
        List<Person> persons = responseEntity.getBody();

        // Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getAccept());
        // Assertions.assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
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
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8"))
                .build();
        // @formatter:on

        // // ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);
        // persons = this.objectMapper.readValue(responseEntity.getBody(), new TypeReference<List<Person>>()
        // {
        // });

        // Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        // persons = Arrays.asList(personArray);

        ResponseEntity<List<Person>> responseEntity =
                restTemplate.exchange("/rest/person/personList", HttpMethod.GET, null, new ParameterizedTypeReference<List<Person>>()
                {
                });
        List<Person> persons = responseEntity.getBody();

        // Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getAccept());
        // Assertions.assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
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
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .build();
        // @formatter:on

        ApiError error = restTemplate.postForObject("/rest/person/personAdd", new Person("Thomas", "Freese"), ApiError.class);
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), error.getHttpStatus());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test041Post()
     */
    @Override
    @Test
    void test041Post() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("admin", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .build();
        // @formatter:on

        HttpEntity<Person> httpEntity = new HttpEntity<>(new Person("Thomas", "Freese"));
        ResponseEntity<ApiError> responseEntity = restTemplate.exchange("/rest/person/personAdd", HttpMethod.POST, httpEntity, ApiError.class);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // @formatter:off
        restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();
        // @formatter:on

        Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        List<Person> persons = Arrays.asList(personArray);

        Assertions.assertNotNull(persons);
        Assertions.assertTrue(persons.size() >= 3);

        Assertions.assertEquals("Thomas", persons.get(persons.size() - 1).getFirstName());
        Assertions.assertEquals("Freese", persons.get(persons.size() - 1).getLastName());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test050UserWithPreAuthJSON()
     */
    @Override
    @Test
    void test050UserWithPreAuthJSON() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new HttpHeaderInterceptor("my-token", "user"),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();
        // @formatter:on

        Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        List<Person> persons = Arrays.asList(personArray);

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
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new HttpHeaderInterceptor("my-token", "user"),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_XML_VALUE+";charset=UTF-8"))
                .build();
        // @formatter:on

        Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        List<Person> persons = Arrays.asList(personArray);

        Assertions.assertNotNull(persons);
        Assertions.assertTrue(persons.size() >= 2);
    }
}

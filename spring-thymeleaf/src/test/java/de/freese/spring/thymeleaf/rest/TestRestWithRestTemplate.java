/**
 * Created: 07.09.2018
 */

package de.freese.spring.thymeleaf.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Resource;
import org.apache.http.client.HttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.devtools.remote.client.HttpHeaderInterceptor;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import com.jayway.jsonpath.JsonPath;
import de.freese.spring.thymeleaf.ThymeleafApplication;
import de.freese.spring.thymeleaf.exception.ApiError;
import de.freese.spring.thymeleaf.model.Person;

/**
 * @author Thomas Freese
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes =
{
        ThymeleafApplication.class
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestRestWithRestTemplate implements RestTestCase
{
    /**
     * @author Thomas Freese
     */
    private class NoOpResponseErrorHandler extends DefaultResponseErrorHandler
    {
        /**
         * Erstellt ein neues {@link NoOpResponseErrorHandler} Object.
         */
        private NoOpResponseErrorHandler()
        {
            super();
        }

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
    private Environment environment = null;

    /**
     *
     */
    @Resource
    private HttpClient httpClient = null;

    /**
    *
    */
    @LocalServerPort
    private int localServerPort = 0;

    /**
     *
     */
    @Resource
    private RestTemplateBuilder restTemplateBuilder = null;

    /**
     * Erstellt ein neues {@link TestRestWithRestTemplate} Object.
     */
    public TestRestWithRestTemplate()
    {
        super();
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Before
    public void beforeTest() throws Exception
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
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test000HealthEndpoint()
     */
    @Override
    @Test
    public void test000HealthEndpoint() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE))
                .build();
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/actuator/health", String.class);

        Assert.assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, responseEntity.getHeaders().getFirst("Content-Type"));

        String status = JsonPath.parse(responseEntity.getBody()).read("$.status");
        Assert.assertEquals("UP", status);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test010UserWithoutLogin()
     */
    @Override
    @Test
    public void test010UserWithoutLogin() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .build();
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);

        Assert.assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test011UserWithWrongPass()
     */
    @Override
    @Test
    public void test011UserWithWrongPass() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pass", StandardCharsets.UTF_8))
                .build();
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);

        Assert.assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test020UserWithWrongRole()
     */
    @Override
    @Test
    public void test020UserWithWrongRole() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("invalid", "pw", StandardCharsets.UTF_8))
                .build();
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);

        Assert.assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test030UserWithLoginJSON()
     */
    @Override
    @Test
    public void test030UserWithLoginJSON() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE))
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

        // Assert.assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, responseEntity.getHeaders().getFirst("Accept"));
        // Assert.assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        Assert.assertNotNull(persons);
        Assert.assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test031UserWithLoginXML()
     */
    @Override
    @Test
    public void test031UserWithLoginXML() throws Exception
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

        // Assert.assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, responseEntity.getHeaders().getFirst("Accept"));
        // Assert.assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        Assert.assertNotNull(persons);
        Assert.assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test040PostWithWrongRole()
     */
    @Override
    @Test
    public void test040PostWithWrongRole() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE))
                .build();
        // @formatter:on

        ApiError error = restTemplate.postForObject("/rest/person/personAdd", new Person("Thomas", "Freese"), ApiError.class);
        Assert.assertEquals(HttpStatus.FORBIDDEN.value(), error.getHttpStatus());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test041Post()
     */
    @Override
    @Test
    public void test041Post() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("admin", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE))
                .build();
        // @formatter:on

        HttpEntity<Person> httpEntity = new HttpEntity<>(new Person("Thomas", "Freese"));
        ResponseEntity<ApiError> responseEntity = restTemplate.exchange("/rest/person/personAdd", HttpMethod.POST, httpEntity, ApiError.class);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // @formatter:off
        restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE))
                .build();
        // @formatter:on

        Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        List<Person> persons = Arrays.asList(personArray);

        Assert.assertNotNull(persons);
        Assert.assertTrue(persons.size() >= 3);

        Assert.assertEquals("Thomas", persons.get(persons.size() - 1).getFirstName());
        Assert.assertEquals("Freese", persons.get(persons.size() - 1).getLastName());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test050UserWithPreAuthJSON()
     */
    @Override
    @Test
    public void test050UserWithPreAuthJSON() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new HttpHeaderInterceptor("my-token", "user"),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE))
                .build();
        // @formatter:on

        Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        List<Person> persons = Arrays.asList(personArray);

        Assert.assertNotNull(persons);
        Assert.assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test051UserWithPreAuthXML()
     */
    @Override
    @Test
    public void test051UserWithPreAuthXML() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new HttpHeaderInterceptor("my-token", "user"),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_XML_VALUE+";charset=UTF-8"))
                .build();
        // @formatter:on

        Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        List<Person> persons = Arrays.asList(personArray);

        Assert.assertNotNull(persons);
        Assert.assertTrue(persons.size() >= 2);
    }
}

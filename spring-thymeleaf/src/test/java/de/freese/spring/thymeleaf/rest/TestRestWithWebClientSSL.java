/**
 * Created: 07.09.2018
 */

package de.freese.spring.thymeleaf.rest;

import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.annotation.Resource;
import javax.net.ssl.TrustManagerFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import de.freese.spring.thymeleaf.ThymeleafApplication;
import de.freese.spring.thymeleaf.model.Person;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes =
{
        ThymeleafApplication.class
})
@AutoConfigureMockMvc
@ActiveProfiles(
{
        "test", "with-ssl"
})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestRestWithWebClientSSL implements RestTestCase
{
    /**
     *
     */
    @Resource
    private Environment environment = null;

    /**
    *
    */
    @LocalServerPort
    private int localServerPort = 0;

    /**
     *
     */
    @Resource
    private ObjectMapper objectMapper = null;

    /**
     *
     */
    @Resource
    private WebClient.Builder webClientBuilder = null;

    /**
     * Erstellt ein neues {@link TestRestWithWebClientSSL} Object.
     */
    public TestRestWithWebClientSSL()
    {
        super();
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Before
    public void beforeTest() throws Exception
    {
        String rootUri = ThymeleafApplication.getRootUri(this.environment);

        // final SslContext sslContext;
        //
        // try
        // {
        // KeyStore trustStore = KeyStore.getInstance("PKCS12");
        // trustStore.load(new FileInputStream(ResourceUtils.getFile("classpath:server_truststore.p12")), "password".toCharArray());
        //
        // List<Certificate> certificateCollection = null;
        //
        // // certificateCollection = Collections.list(trustStore.aliases()).stream().filter(alias -> {
        // // try
        // // {
        // // return trustStore.isCertificateEntry(alias);
        // // }
        // // catch (KeyStoreException ex)
        // // {
        // // throw new RuntimeException("Error reading truststore", ex);
        // // }
        // // }).map(alias -> {
        // // try
        // // {
        // // return trustStore.getCertificate(alias);
        // // }
        // // catch (KeyStoreException ex)
        // // {
        // // throw new RuntimeException("Error reading truststore", ex);
        // // }
        // // }).collect(Collectors.toList());
        //
        // certificateCollection = Arrays.asList(trustStore.getCertificate("localhost"));
        //
        // KeyStore keyStore = KeyStore.getInstance("PKCS12");
        // keyStore.load(new FileInputStream(ResourceUtils.getFile("classpath:server_keystore.p12")), "password".toCharArray());
        //
        // // .trustManager(certificateCollection.toArray(new X509Certificate[certificateCollection.size()]))
        // sslContext = SslContextBuilder.forClient().keyManager((PrivateKey) keyStore.getKey("localhost", "password".toCharArray()))
        // .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        // }
        // catch (Exception ex)
        // {
        // // log.error("Error creating web client", e);
        // throw new RuntimeException(ex);
        // }

        // @formatter:off
//        ExchangeStrategies strategies = ExchangeStrategies.builder()
//                .codecs(configurer -> {
//                    configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(this.objectMapper, MediaType.APPLICATION_JSON));
//                    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(this.objectMapper, MediaType.APPLICATION_JSON));
//
//                }).build();

         TrustManagerFactory trustManagerFactory = InsecureTrustManagerFactory.INSTANCE;
//        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
//        trustManagerFactory.init(trustStore);

        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(trustManagerFactory)
                .build();

        ClientHttpConnector httpConnector = new ReactorClientHttpConnector(opt -> opt.sslContext(sslContext));

        this.webClientBuilder.baseUrl(rootUri)
            .clientConnector(httpConnector)
            //.exchangeStrategies(strategies)
            ;
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
        WebClient webClient = this.webClientBuilder.build();

//        RequestHeadersSpec<?> request = webClient.get()
//                .uri("/actuator/health")
//                .accept(MediaType.APPLICATION_JSON_UTF8)
//                ;

//        Mono<String> response = webClient.get()
//                .uri("/actuator/health")
//                .accept(MediaType.APPLICATION_JSON_UTF8)
//                .retrieve() // Liefert nur den Body.
//                .bodyToMono(String.class)
//                ;

//        Mono<String> response = webClient.get()
//                .uri("/actuator/health")
//                .accept(MediaType.APPLICATION_JSON_UTF8)
//                .exchange() // Liefert auch Header und Status.
//                .doOnSuccess(clientResponse -> Assert.assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, clientResponse.headers().asHttpHeaders().getFirst("Content-Type")))
//                .flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
//                ;

        Mono<ResponseEntity<String>> response = webClient.get()
                .uri("/actuator/health")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange() // Liefert auch Header und Status.
                .flatMap(clientResponse -> clientResponse.toEntity(String.class))
                ;
        // @formatter:on

        ResponseEntity<String> responseEntity = response.block();

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
        WebClient webClient = this.webClientBuilder.build();

        // @formatter:off
//        Flux<Person> personFlux = webClient.get()
//                .uri("/rest/person/personList")
//                .accept(MediaType.APPLICATION_JSON_UTF8)
//                .retrieve()
//                .onStatus(status ->  !HttpStatus.UNAUTHORIZED.equals(status) , response -> Mono.just(new Exception()))
//                .bodyToFlux(Person.class)

//                .exchange()
//                .flatMapIterable(clientResponse -> clientResponse.bodyToFlux(Person.class))
//                ;
        Mono<ResponseEntity<String>> response = webClient.get()
                .uri("/rest/person/personList")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .flatMap(clientResponse -> clientResponse.toEntity(String.class))
                ;
        // @formatter:on

        ResponseEntity<String> responseEntity = response.block();

        Assert.assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test011UserWithWrongPass()
     */
    @Override
    @Test
    public void test011UserWithWrongPass() throws Exception
    {
        WebClient webClient = this.webClientBuilder.build();

        // @formatter:off
//        Flux<Person> personFlux = webClient.mutate()
//                .filter(ExchangeFilterFunctions.basicAuthentication("user", "pass"))
//                .build()
//                .get()
//                .uri("/rest/person/personList")
//                .accept(MediaType.APPLICATION_JSON_UTF8)
//                .retrieve()
//                .onStatus(status ->  !HttpStatus.UNAUTHORIZED.equals(status), response -> Mono.just(new Exception()))
//                .bodyToFlux(Person.class)
//                ;
        Mono<ResponseEntity<String>> response = webClient.mutate()
                .filter(ExchangeFilterFunctions.basicAuthentication("user", "pass"))
                .build()
                .get()
                .uri("/rest/person/personList")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .flatMap(clientResponse -> clientResponse.toEntity(String.class))
                ;
        // @formatter:on

        ResponseEntity<String> responseEntity = response.block();

        Assert.assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test020UserWithWrongRole()
     */
    @Override
    @Test
    public void test020UserWithWrongRole() throws Exception
    {
        WebClient webClient = this.webClientBuilder.build();

        // @formatter:off
        Mono<ResponseEntity<String>> response = webClient.mutate()
                .filter(ExchangeFilterFunctions.basicAuthentication("invalid", "pw"))
                .build()
                .get()
                .uri("/rest/person/personList")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .flatMap(clientResponse -> clientResponse.toEntity(String.class))
                ;
        // @formatter:on

        ResponseEntity<String> responseEntity = response.block();

        Assert.assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test030UserWithLoginJSON()
     */
    @Override
    @Test
    public void test030UserWithLoginJSON() throws Exception
    {
        WebClient webClient = this.webClientBuilder.build();

        // @formatter:off
        Flux<Person> personFlux = webClient.mutate()
                   .filter(ExchangeFilterFunctions.basicAuthentication("user", "pw"))
                   .build()
                   .get()
                   .uri("/rest/person/personList")
                   .accept(MediaType.APPLICATION_JSON_UTF8)
                   .retrieve()
                   .bodyToFlux(Person.class)
                   ;
        // @formatter:on

        List<Person> persons = personFlux.collectList().block();

        Assert.assertNotNull(persons);
        Assert.assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test031UserWithLoginXML()
     */
    @Override
    // @Test
    public void test031UserWithLoginXML() throws Exception
    {
        WebClient webClient = this.webClientBuilder.build();

        // @formatter:off
        Flux<Person> personFlux = webClient.mutate()
                   .filter(ExchangeFilterFunctions.basicAuthentication("user", "pw"))
                   .build()
                   .get()
                   .uri("/rest/person/personList")
                   .accept(MediaType.APPLICATION_XML)
                   .acceptCharset(StandardCharsets.UTF_8)
                   //.header("Accept", MediaType.APPLICATION_XML_VALUE+";charset=UTF-8")
                   .retrieve()
                   .bodyToFlux(Person.class)
                   ;
        // @formatter:on

        List<Person> persons = personFlux.collectList().block();

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
        WebClient webClient = this.webClientBuilder.build();
        Person newPerson = new Person("Thomas", "Freese");

        // @formatter:off
        Mono<ResponseEntity<String>> response = webClient.mutate()
                .filter(ExchangeFilterFunctions.basicAuthentication("user", "pw"))
                .build()
                .post()
                .uri("/rest/person/personList")
                .contentType(MediaType.APPLICATION_JSON)
                //.body(Mono.just(newPerson), Person.class)
                .syncBody(newPerson)
                .exchange()
                .flatMap(clientResponse -> clientResponse.toEntity(String.class))
                ;
        // @formatter:on

        // ResponseEntity<ApiError> responseEntity = response.block();
        ResponseEntity<String> responseEntity = response.block();
        // ApiError apiError = responseEntity.getBody();

        // Assert.assertEquals(HttpStatus.FORBIDDEN.value(), apiError.getHttpStatus());
        Assert.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, responseEntity.getStatusCode());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test041Post()
     */
    @Override
    @Test
    public void test041Post() throws Exception
    {
        WebClient webClient = this.webClientBuilder.build();
        Person newPerson = new Person("Thomas", "Freese");

        // @formatter:off
        Mono<ResponseEntity<String>> response = webClient.mutate()
                .filter(ExchangeFilterFunctions.basicAuthentication("admin", "pw"))
                .build()
                .post()
                .uri("/rest/person/personAdd")
                .contentType(MediaType.APPLICATION_JSON)
                //.body(Mono.just(newPerson), Person.class)
                .syncBody(newPerson)
                .exchange()
                .flatMap(clientResponse -> clientResponse.toEntity(String.class))
                ;
        // @formatter:on

        // ResponseEntity<ApiError> responseEntity = response.block();
        ResponseEntity<String> responseEntity = response.block();
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // @formatter:off
        Flux<Person> personFlux = webClient.mutate()
                   .filter(ExchangeFilterFunctions.basicAuthentication("user", "pw"))
                   .build()
                   .get()
                   .uri("/rest/person/personList")
                   .accept(MediaType.APPLICATION_JSON_UTF8)
                   .retrieve()
                   .bodyToFlux(Person.class)
                   ;
        // @formatter:on

        List<Person> persons = personFlux.collectList().block();

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
        WebClient webClient = this.webClientBuilder.build();

        // @formatter:off
        Flux<Person> personFlux = webClient
                   .get()
                   .uri("/rest/person/personList")
                   .accept(MediaType.APPLICATION_JSON)
                   .acceptCharset(StandardCharsets.UTF_8)
                   .header("my-token", "user")
                   .retrieve()
                   .bodyToFlux(Person.class)
                   ;
        // @formatter:on

        List<Person> persons = personFlux.collectList().block();

        Assert.assertNotNull(persons);
        Assert.assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test051UserWithPreAuthXML()
     */
    @Override
    // @Test
    public void test051UserWithPreAuthXML() throws Exception
    {
        WebClient webClient = this.webClientBuilder.build();

        // @formatter:off
        Flux<Person> personFlux = webClient
                   .get()
                   .uri("/rest/person/personList")
                   .accept(MediaType.APPLICATION_XML)
                   .acceptCharset(StandardCharsets.UTF_8)
                   .header("my-token", "user")
                   .retrieve()
                   .bodyToFlux(Person.class)
                   ;
        // @formatter:on

        List<Person> persons = personFlux.collectList().block();

        Assert.assertNotNull(persons);
        Assert.assertTrue(persons.size() >= 2);
    }
}

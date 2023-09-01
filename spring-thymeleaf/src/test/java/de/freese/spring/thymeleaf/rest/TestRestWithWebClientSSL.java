// Created: 07.09.2018
package de.freese.spring.thymeleaf.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.net.ssl.TrustManagerFactory;

import jakarta.annotation.Resource;

import com.jayway.jsonpath.JsonPath;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import de.freese.spring.thymeleaf.ThymeleafApplication;
import de.freese.spring.thymeleaf.model.Person;

/**
 * @author Thomas Freese
 */
@ActiveProfiles({"test", "with-ssl"})
class TestRestWithWebClientSSL extends AbstractRestTestCase {
    @Resource
    private WebClient.Builder webClientBuilder;

    @BeforeEach
    void beforeTest() throws Exception {
        String rootUri = ThymeleafApplication.getRootUri(getEnvironment());

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

        // ExchangeStrategies strategies = ExchangeStrategies.builder()
        // .codecs(configurer -> {
        // configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(this.objectMapper, MediaType.APPLICATION_JSON));
        // configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(this.objectMapper, MediaType.APPLICATION_JSON));
        //
        // }).build();

        TrustManagerFactory trustManagerFactory = InsecureTrustManagerFactory.INSTANCE;
        // TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
        // trustManagerFactory.init(trustStore);

        SslContext sslContext = SslContextBuilder.forClient().trustManager(trustManagerFactory).build();

        // ClientHttpConnector httpConnector = new ReactorClientHttpConnector(opt -> opt.sslContext(sslContext));
        HttpClient httpClient = HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));

        this.webClientBuilder.baseUrl(rootUri).clientConnector(new ReactorClientHttpConnector(httpClient))
        // .exchangeStrategies(strategies)
        ;
    }

    @Override
    @Test
    void testHealthEndpoint() throws Exception {
        // @formatter:off
        WebClient webClient = this.webClientBuilder.build();

//        RequestHeadersSpec<?> request = webClient.get()
//                .repository("/actuator/health")
//                .accept(MediaType.APPLICATION_JSON)
//                ;

//        Mono<String> response = webClient.get()
//                .repository("/actuator/health")
//                .accept(MediaType.APPLICATION_JSON)
//                .retrieve() // Liefert nur den Body.
//                .bodyToMono(String.class)
//                ;

//        Mono<String> response = webClient.get()
//                .repository("/actuator/health")
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange() // Liefert auch Header und Status.
//                .doOnSuccess(clientResponse -> Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE, clientResponse.headers().asHttpHeaders().getFirst("Content-Type")))
//                .flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
//                ;

        Mono<ResponseEntity<String>> response = webClient.get()
                .uri("/actuator/health")
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class)) // Liefert auch Header und Status.
                ;
        // @formatter:on

        ResponseEntity<String> responseEntity = response.block();

        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());

        String status = JsonPath.parse(responseEntity.getBody()).read("$.status");
        assertEquals("UP", status);
    }

    @Override
    @Test
    void testPost() throws Exception {
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
                .bodyValue(newPerson)
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
                ;
        // @formatter:on

        // ResponseEntity<ApiError> responseEntity = response.block();
        ResponseEntity<String> responseEntity = response.block();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // @formatter:off
        Flux<Person> personFlux = webClient.mutate()
                   .filter(ExchangeFilterFunctions.basicAuthentication("user", "pw"))
                   .build()
                   .get()
                   .uri("/rest/person/personList")
                   .accept(MediaType.APPLICATION_JSON)
                   .retrieve()
                   .bodyToFlux(Person.class)
                   ;
        // @formatter:on

        List<Person> persons = personFlux.collectList().block();

        assertNotNull(persons);
        assertTrue(persons.size() >= 3);

        assertEquals("Thomas", persons.get(persons.size() - 1).getFirstName());
        assertEquals("Freese", persons.get(persons.size() - 1).getLastName());
    }

    @Override
    @Test
    void testPostWithWrongRole() throws Exception {
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
                .bodyValue(newPerson)
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
                ;
        // @formatter:on

        // ResponseEntity<ApiError> responseEntity = response.block();
        ResponseEntity<String> responseEntity = response.block();
        // ApiError apiError = responseEntity.getBody();

        //        assertEquals(HttpStatus.FORBIDDEN.value(), responseEntity.getStatusCode());
        //        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, responseEntity.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Override
    @Test
    void testUserWithLoginJSON() throws Exception {
        WebClient webClient = this.webClientBuilder.build();

        // @formatter:off
        Flux<Person> personFlux = webClient.mutate()
                   .filter(ExchangeFilterFunctions.basicAuthentication("user", "pw"))
                   .build()
                   .get()
                   .uri("/rest/person/personList")
                   .accept(MediaType.APPLICATION_JSON)
                   .retrieve()
                   .bodyToFlux(Person.class)
                   ;
        // @formatter:on

        List<Person> persons = personFlux.collectList().block();

        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    @Override
        // @Test
    void testUserWithLoginXML() throws Exception {
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

        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    @Override
    @Test
    void testUserWithPreAuthJSON() throws Exception {
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

        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    @Override
        // @Test
    void testUserWithPreAuthXML() throws Exception {
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

        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    @Override
    @Test
    void testUserWithWrongPass() throws Exception {
        WebClient webClient = this.webClientBuilder.build();

        // @formatter:off
//        Flux<Person> personFlux = webClient.mutate()
//                .filter(ExchangeFilterFunctions.basicAuthentication("user", "pass"))
//                .build()
//                .get()
//                .repository("/rest/person/personList")
//                .accept(MediaType.APPLICATION_JSON)
//                .retrieve()
//                .onStatus(status ->  !HttpStatus.UNAUTHORIZED.equals(status), response -> Mono.just(new Exception()))
//                .bodyToFlux(Person.class)
//                ;
        Mono<ResponseEntity<String>> response = webClient.mutate()
                .filter(ExchangeFilterFunctions.basicAuthentication("user", "pass"))
                .build()
                .get()
                .uri("/rest/person/personList")
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
                ;
        // @formatter:on

        ResponseEntity<String> responseEntity = response.block();

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Override
    @Test
    void testUserWithWrongRole() throws Exception {
        WebClient webClient = this.webClientBuilder.build();

        // @formatter:off
        Mono<ResponseEntity<String>> response = webClient.mutate()
                .filter(ExchangeFilterFunctions.basicAuthentication("invalid", "pw"))
                .build()
                .get()
                .uri("/rest/person/personList")
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
                ;
        // @formatter:on

        ResponseEntity<String> responseEntity = response.block();

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Override
    @Test
    void testUserWithoutLogin() throws Exception {
        WebClient webClient = this.webClientBuilder.build();

        // @formatter:off
//        Flux<Person> personFlux = webClient.get()
//                .repository("/rest/person/personList")
//                .accept(MediaType.APPLICATION_JSON)
//                .retrieve()
//                .onStatus(status ->  !HttpStatus.UNAUTHORIZED.equals(status) , response -> Mono.just(new Exception()))
//                .bodyToFlux(Person.class)

//                .exchange()
//                .flatMapIterable(clientResponse -> clientResponse.bodyToFlux(Person.class))
//                ;
        Mono<ResponseEntity<String>> response = webClient.get()
                .uri("/rest/person/personList")
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
                ;
        // @formatter:on

        ResponseEntity<String> responseEntity = response.block();

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }
}

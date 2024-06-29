// Created: 16.08.23
package de.freese.spring.data.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.annotation.Resource;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.io.ManagedHttpClientConnection;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.io.HttpConnectionFactory;
import org.apache.hc.core5.http.io.entity.HttpEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import de.freese.spring.data.jpa.domain.Status;
import de.freese.spring.data.jpa.domain.Todo;
import de.freese.spring.data.jpa.exception.ApplicationException;
import de.freese.spring.data.jpa.infrastructure.MyHibernateRepository;

/**
 * <pre>{@code
 * @ExtendWith(SpringExtension.class)
 * @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
 * @Import(WebConfig.class)
 * properties = "server.port=0",
 * }</pre>
 *
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TodoApplicationTests {

    @LocalServerPort
    private int localServerPort;
    @Resource
    private MyHibernateRepository myHibernateRepository;
    @Resource
    private RestClient.Builder restClientBuilder;
    @Resource
    private WebTestClient webTestClient;

    @BeforeEach
    void beforeEach() {
        // webTestClient = WebTestClient.bindToController(new TodoController(new TodoService(repository))).build();
        // webTestClient = WebTestClient
        //         .bindToServer()
        //         .baseUrl("http://localhost:" + localServerPort)
        //         .codecs(configurer -> {
        //             // configurer.registerDefaults(true);
        //             configurer.defaultCodecs().jaxb2Encoder(new Jaxb2XmlEncoder());
        //             configurer.defaultCodecs().jaxb2Decoder(new Jaxb2XmlDecoder());
        //             // configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder());
        //             // configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder());
        //         }).build();
    }

    @Test
    void testGetAllTodosJson() {
        testCreateTodo();

        webTestClient.get()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(value -> {
                    System.out.println(value);
                    assertNotNull(value);
                })
        ;

        webTestClient.get()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Todo.class).value(list -> {
                    System.out.println(list);
                    assertNotNull(list);
                    assertFalse(list.isEmpty());
                })
        ;

        // final Function<ClientResponse, Mono<ClientResponse>> exceptionFilterFunction = clientResponse -> {
        //     final HttpStatus statusCode = HttpStatus.resolve(clientResponse.statusCode().value());
        //
        //     if (!HttpStatus.OK.equals(statusCode)) {
        //         return clientResponse.bodyToMono(String.class).flatMap(body -> Mono.error(new ApplicationException(body)));
        //     }
        //
        //     return Mono.just(clientResponse);
        // };

        final WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:" + localServerPort)
                // .filter(ExchangeFilterFunction.ofResponseProcessor(exceptionFilterFunction))
                .build();

        webClient.get()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                // .onStatus(status -> status != HttpStatus.OK, clientResponse -> Mono.empty())
                .onStatus(status -> status != HttpStatus.OK, clientResponse -> clientResponse.bodyToMono(String.class).map(ApplicationException::new))
                .bodyToFlux(Todo.class)
                // .exchangeToFlux(response -> response.bodyToFlux(Todo.class))
                // .blockOptional().ifPresent(System.out::println)
                .doOnNext(value -> {
                    System.out.println(value);
                    assertNotNull(value);
                }).blockLast();
    }

    @Test
    void testGetAllTodosXml() {
        testCreateTodo();

        webTestClient.get()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(value -> {
                    System.out.println(value);
                    assertNotNull(value);
                })
        ;

        webTestClient.get()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Todo.class).value(list -> {
                    System.out.println(list);
                    assertNotNull(list);
                    // assertFalse(list.isEmpty());
                })
        ;
    }

    @Test
    void testMyHibernateRepository() {
        assertNotNull(myHibernateRepository);
        assertNotNull(myHibernateRepository.getEntityManagerFactory());
        assertNotNull(myHibernateRepository.getSessionFactory());
    }

    @Test
    void testNotFoundRestClient() {
        final RestClient restClient = restClientBuilder.baseUrl("http://localhost:" + localServerPort).build();

        final HttpClientErrorException exception = assertThrows(HttpClientErrorException.NotFound.class,
                () -> restClient.get()
                        .uri("/api/todo/" + UUID.randomUUID())
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        // .onStatus(new DefaultResponseErrorHandler() {
                        //     @Override
                        //     public boolean hasError(final ClientHttpResponse response) throws IOException {
                        //         return false;
                        //     }
                        // })
                        .body(Todo.class));

        assertNotNull(exception);
        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getStatusCode().value());

        // final ProblemDetail problemDetail = exception.getResponseBodyAs(ProblemDetail.class);
        final String problemDetail = exception.getResponseBodyAs(String.class);
        System.out.println(problemDetail);
        assertNotNull(problemDetail);
        assertTrue(problemDetail.contains("Todo not found by ID:"));
    }

    @Test
    void testNotFoundWebTestClient() {
        webTestClient.get()
                .uri("/api/todo/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                // .expectBody(ProblemDetail.class).value(System.out::println)
                .expectBody(String.class).value(value -> {
                    System.out.println(value);
                    assertNotNull(value);
                    assertTrue(value.contains("Todo not found by ID:"));
                })
        ;
    }

    @Test
    void testStreams() {
        // restClient.post().uri("...").body(outputStream->{});
        webTestClient.post()
                .uri("/api/todo/" + UUID.randomUUID() + "/stream")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(BodyInserters.fromResource(new InputStreamResource(new ByteArrayInputStream("From Client: Hello World".getBytes(StandardCharsets.UTF_8)))))
                // .bodyValue("From Client: Hello World".getBytes(StandardCharsets.UTF_8)) // Alternative
                .exchange()
                .expectStatus()
                .isOk()
        ;

        // restClient.get().uri("...").retrieve().body(InputStreamResource.class);
        webTestClient.get()
                .uri("/api/todo/" + UUID.randomUUID() + "/stream")
                .accept(MediaType.APPLICATION_OCTET_STREAM).exchange()
                .expectStatus()
                .isOk()
                .expectBody(InputStreamResource.class)
                .value(inputStreamResource -> {
                    // Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    // BufferedReader bufferedReader = new BufferedReader(reader)
                    try (InputStream inputStream = inputStreamResource.getInputStream()) {
                        // final String message = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
                        final String message = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                        assertEquals("From Server: Hello World", message);
                        System.out.println(message);
                    }
                    catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                })
        ;
    }

    @Test
    void testStreamsApacheHttp() throws IOException {
        final String url = "http://localhost:" + localServerPort + "/api/todo/" + UUID.randomUUID() + "/stream";

        try (CloseableHttpClient httpClient = createApacheHttp()) {
            try (HttpEntity httpEntity = HttpEntities.create(outputStream -> {
                        outputStream.write("From Client: Hello World".getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                    },
                    ContentType.APPLICATION_OCTET_STREAM)) {

                final HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader("Content-Type", "application/octet-stream");
                httpPost.setEntity(httpEntity);

                httpClient.execute(httpPost, response -> {
                    final int responseCode = response.getCode();
                    final String reasonPhrase = response.getReasonPhrase();

                    assertEquals(200, responseCode);
                    assertEquals("", reasonPhrase);

                    return null;
                });

                // try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                //     final int responseCode = response.getCode();
                //     final String reasonPhrase = response.getReasonPhrase();
                //
                //     assertEquals(200, responseCode);
                //     assertEquals("", reasonPhrase);
                // }
            }

            final HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Accept", "application/octet-stream");

            httpClient.execute(httpGet, response -> {
                final int responseCode = response.getCode();
                final String reasonPhrase = response.getReasonPhrase();

                assertEquals(200, responseCode);
                assertEquals("", reasonPhrase);

                // Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                // BufferedReader bufferedReader = new BufferedReader(reader)
                try (InputStream inputStream = response.getEntity().getContent()) {

                    // final String message = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
                    final String message = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    assertEquals("From Server: Hello World", message);

                    System.out.println(message);
                }

                return null;
            });

            // try (CloseableHttpResponse response = httpClient.execute(httpGet);
            //      InputStream inputStream = response.getEntity().getContent();
            //     final int responseCode = response.getCode();
            //     final String reasonPhrase = response.getReasonPhrase();
            //
            //     assertEquals(200, responseCode);
            //     assertEquals("", reasonPhrase);
            //
            //     final String message = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            //     assertEquals("From Server: Hello World", message);
            // }
        }
    }

    @Test
    void testStreamsRestClient() throws IOException {
        final RestClient restClient = restClientBuilder.baseUrl("http://localhost:" + localServerPort).build();

        final ResponseEntity<String> responseEntityString = restClient.post()
                .uri("/api/todo/" + UUID.randomUUID() + "/stream")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(outputStream -> {
                    outputStream.write("From Client: Hello World".getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                })
                .retrieve()
                .toEntity(String.class);

        assertEquals(200, responseEntityString.getStatusCode().value());
        assertNull(responseEntityString.getBody());

        final String message = restClient.get()
                .uri("/api/todo/" + UUID.randomUUID() + "/stream")
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .exchange((request, response) -> {
                    return new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    // if (response.getStatusCode().equals(HttpStatus.OK)) {
                    //     return deserialize(response.getBody());
                    // }
                    // else {
                    //     throw new BusinessException();
                    // }
                });
        assertNotNull(message);
        assertEquals("From Server: Hello World", message);

        // final byte[] bytes = restClient.get()
        //         .uri("/api/todo/" + UUID.randomUUID() + "/stream")
        //         .accept(MediaType.APPLICATION_OCTET_STREAM)
        //         .exchange((request, response) -> response.getBody()).readAllBytes();
        // assertNotNull(bytes);
        // assertEquals("From Server: Hello World", new String(bytes, StandardCharsets.UTF_8));

        // java.io.IOException: closed
        // final InputStream inputStream = restClient.get()
        //         .uri("/api/todo/" + UUID.randomUUID() + "/stream")
        //         .accept(MediaType.APPLICATION_OCTET_STREAM)
        //         .exchange((request, response) -> response.getBody());
        // assertNotNull(inputStream);
        //
        // try (inputStream) {
        //     assertEquals("From Server: Hello World", new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        // }

        // java.io.IOException: closed
        // final InputStreamResource inputStreamResource = restClient.get()
        //         .uri("/api/todo/" + UUID.randomUUID() + "/stream")
        //         .accept(MediaType.APPLICATION_OCTET_STREAM)
        //         .retrieve()
        //         .body(InputStreamResource.class);
        // assertNotNull(inputStreamResource);
        //
        // try (InputStream inputStream = inputStreamResource.getInputStream()) {
        //     assertEquals("From Server: Hello World", new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        // }

        // java.io.IOException: closed
        // final ResponseEntity<InputStreamResource> responseEntityStream = restClient.get()
        //         .uri("/api/todo/" + UUID.randomUUID() + "/stream")
        //         .retrieve()
        //         .toEntity(InputStreamResource.class);
        //
        // assertEquals(200, responseEntityStream.getStatusCode().value());
        // assertNotNull(responseEntityStream.getBody());
        //
        // try (InputStream inputStream = responseEntityStream.getBody().getInputStream()) {
        //     assertEquals("From Server: Hello World", new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        // }
    }

    private CloseableHttpClient createApacheHttp() {
        final int chunkSize = 1_048_576;

        final Http1Config http1Config = Http1Config.custom().setChunkSizeHint(chunkSize).setBufferSize(chunkSize).build();
        final CharCodingConfig charCodingConfig = CharCodingConfig.custom().setCharset(StandardCharsets.UTF_8).build();

        final HttpConnectionFactory<ManagedHttpClientConnection> connectionSocketFactory = new ManagedHttpClientConnectionFactory(http1Config, charCodingConfig, null);

        // final SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(SSLContext.getDefault(), HttpsURLConnection.getDefaultHostnameVerifier());

        final HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setConnectionFactory(connectionSocketFactory)
                // .setSSLSocketFactory(sslConnectionSocketFactory)
                // .setMaxConnPerRoute(5)
                // .setMaxConnTotal(20)
                .build();

        return HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .evictExpiredConnections()
                .build();
    }

    private void testCreateTodo() {
        final Todo todo = new Todo();
        todo.setName("Test");
        todo.setStartTime(LocalDateTime.now());
        todo.setEndTime(LocalDateTime.now().plusDays(1));
        todo.setTaskStatus(Status.PENDING);

        webTestClient.post()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(todo)).exchange()
                .expectStatus().isCreated()
                .expectBody(Todo.class).value(value -> {
                    System.out.println(value);
                    assertNotNull(value);
                });
    }
}

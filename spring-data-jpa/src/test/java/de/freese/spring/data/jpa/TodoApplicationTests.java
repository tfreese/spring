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
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import jakarta.annotation.Resource;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.HttpEntities;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

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
@SuppressWarnings("java:S1135")
class TodoApplicationTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(TodoApplicationTests.class);

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
                    LOGGER.info(value);
                    assertNotNull(value);
                })
        ;

        webTestClient.get()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Todo.class).value(list -> {
                    LOGGER.info(list.toString());
                    assertNotNull(list);
                    assertFalse(list.isEmpty());
                })
        ;

        final List<Todo> todos = createTodoClient().getAllTodos();
        assertNotNull(todos);
        assertFalse(todos.isEmpty());

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
                    LOGGER.info(String.valueOf(value));
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
                    LOGGER.info(value);
                    assertNotNull(value);
                })
        ;

        webTestClient.get()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Todo.class).value(list -> {
                    LOGGER.info(list.toString());
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

        final Consumer<HttpClientErrorException> exceptionTester = exception -> {
            assertNotNull(exception);
            assertEquals(HttpStatus.NOT_FOUND.value(), exception.getStatusCode().value());

            // final ProblemDetail problemDetail = exception.getResponseBodyAs(ProblemDetail.class);
            final String problemDetail = exception.getResponseBodyAs(String.class);
            LOGGER.info(problemDetail);
            assertNotNull(problemDetail);
            assertTrue(problemDetail.contains("Todo not found by ID:"));
        };

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
        exceptionTester.accept(exception);

        final HttpClientErrorException exception2 = assertThrows(HttpClientErrorException.NotFound.class,
                () -> createTodoClient().getTodoById(UUID.randomUUID()));
        exceptionTester.accept(exception2);
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
                    LOGGER.info(value);
                    assertNotNull(value);
                    assertTrue(value.contains("Todo not found by ID:"));
                })
        ;
    }

    @Test
    void testStreams() throws IOException {
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
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(InputStreamResource.class)
                .value(inputStreamResource -> {
                    // inputStreamResource.getContentAsString(StandardCharsets.UTF_8);
                    // Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    // BufferedReader bufferedReader = new BufferedReader(reader)
                    try (InputStream inputStream = inputStreamResource.getInputStream()) {
                        // final String message = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
                        final String message = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                        assertEquals("From Server: Hello World", message);
                        LOGGER.info(message);
                    }
                    catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                })
        ;

        final TodoClient todoClient = createTodoClient();

        try (InputStream inputStream = new ByteArrayInputStream("From Client: Hello World".getBytes(StandardCharsets.UTF_8))) {
            final ResponseEntity<Void> responseEntity = todoClient.putStream(UUID.randomUUID(), new InputStreamResource(inputStream));

            assertNotNull(responseEntity);
            assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCode().value());
        }

        // ByteArrayResource NOT InputStreamResource !!!
        // final InputStreamResource inputStreamResource = todoClient.getStream(UUID.randomUUID());
        // assertNotNull(inputStreamResource);

        final ResponseEntity<org.springframework.core.io.Resource> responseEntity = todoClient.getStream(UUID.randomUUID());
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCode().value());

        final org.springframework.core.io.Resource resource = responseEntity.getBody();
        // final org.springframework.core.io.Resource resource = inputStreamResource;

        try (InputStream inputStream = resource.getInputStream()) {
            final String message = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("From Server: Hello World", message);
            LOGGER.info(message);
        }
    }

    /**
     * <a href=https://github.com/apache/httpcomponents-client/blob/5.4.x/httpclient5/src/test/java/org/apache/hc/client5/http/examples/ClientConfiguration.java>config</a>
     */
    @Test
    void testStreamsApacheHttp() throws IOException {
        final String url = "http://localhost:" + localServerPort + "/api/todo/" + UUID.randomUUID() + "/stream";

        try (CloseableHttpClient httpClient = createApacheHttp()) {
            try (HttpEntity httpEntity = HttpEntities.create(outputStream -> {
                        outputStream.write("From Client: Hello World".getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                    },
                    ContentType.APPLICATION_OCTET_STREAM)) {

                final ClassicHttpRequest httpRequest = ClassicRequestBuilder
                        .post(url)
                        .setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM.getMimeType())
                        .setEntity(httpEntity)
                        .build();

                httpClient.execute(httpRequest, response -> {
                    final int responseCode = response.getCode();
                    final String reasonPhrase = response.getReasonPhrase();

                    assertEquals(HttpURLConnection.HTTP_OK, responseCode);
                    assertEquals("", reasonPhrase);
                    assertEquals("", EntityUtils.toString(response.getEntity()));

                    return null;
                });
            }

            final ClassicHttpRequest httpRequest = ClassicRequestBuilder
                    .get(url)
                    .setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_OCTET_STREAM.getMimeType())
                    .build();

            // BasicHttpClientResponseHandler
            httpClient.execute(httpRequest, response -> {
                final int responseCode = response.getCode();
                final String reasonPhrase = response.getReasonPhrase();

                assertEquals(org.apache.hc.core5.http.HttpStatus.SC_OK, responseCode);
                assertEquals("", reasonPhrase);
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
    }

    @Test
    void testStreamsRestClient() {
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
                .exchange((request, response) -> new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8));
        assertNotNull(message);
        assertEquals("From Server: Hello World", message);
    }

    /**
     * <a href=https://github.com/apache/httpcomponents-client/blob/5.4.x/httpclient5/src/test/java/org/apache/hc/client5/http/examples/ClientConfiguration.java>config</a>
     */
    private CloseableHttpClient createApacheHttp() {
        return ApacheHttpClientConfigurer.createCloseableHttpClient(3, Duration.ofSeconds(3), null);
    }

    private TodoClient createTodoClient() {
        final RestClient restClient = restClientBuilder.baseUrl("http://localhost:" + localServerPort).build();
        final RestClientAdapter clientAdapter = RestClientAdapter.create(restClient);
        final HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory.builderFor(clientAdapter).build();

        return proxyFactory.createClient(TodoClient.class);
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
                    LOGGER.info(String.valueOf(value));
                    assertNotNull(value);
                });

        final TodoClient todoClient = createTodoClient();

        final Todo todoResponse = todoClient.createTodo(todo);
        assertNotNull(todoResponse);
        assertEquals(todo.getName(), todoResponse.getName());
        assertEquals(todo.getStartTime(), todoResponse.getStartTime());
        assertEquals(todo.getEndTime(), todoResponse.getEndTime());
        assertEquals(todo.getTaskStatus(), todoResponse.getTaskStatus());
    }
}

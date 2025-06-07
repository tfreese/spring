// Created: 22.05.2018
package de.freese.spring.kryo;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MimeType;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import de.freese.spring.kryo.web.KryoHttpMessageConverter;
import de.freese.spring.kryo.webflux.AbstractKryoCodecSupport;
import de.freese.spring.kryo.webflux.KryoDecoder;
import de.freese.spring.kryo.webflux.KryoEncoder;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = KryoApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TestKryo {
    static void validateLocalDateTime(final LocalDateTime localDateTime) {
        Assertions.assertNotNull(localDateTime);

        final LocalDateTime now = LocalDateTime.now();

        Assertions.assertEquals(localDateTime.getYear(), now.getYear());
        Assertions.assertEquals(localDateTime.getMonth().getValue(), now.getMonth().getValue());
        Assertions.assertEquals(localDateTime.getDayOfMonth(), now.getDayOfMonth());
        Assertions.assertEquals(localDateTime.getHour(), now.getHour(), 1);
        Assertions.assertEquals(localDateTime.getMinute(), now.getMinute(), 1);
        // Assertions.assertEquals(localDateTime.getSecond(), now.getSecond());
    }

    private HttpClient.Builder httpClientbuilder;

    @LocalServerPort
    private int localServerPort;

    @Resource
    private MockMvc mockMvc;

    // @Resource
    // private RestTemplateBuilder restTemplateBuilder;

    @Resource
    private ObjectMapper objectMapper;

    private RestTemplate restTemplate;

    @Resource
    private WebApplicationContext webApplicationContext;

    @Resource
    private WebClient.Builder webClientBuilder;

    @Test
    void testHttpClient() throws Exception {
        testHttpClient("/kryo", AbstractKryoCodecSupport.APPLICATION_KRYO);
        testHttpClient("/json", MediaType.APPLICATION_JSON);
    }

    @Test
    void testMockMvc() throws Exception {
        testMockMvc("/kryo", KryoHttpMessageConverter.APPLICATION_KRYO);
        testMockMvc("/json", MediaType.APPLICATION_JSON);
    }

    @Test
    void testRestTemplate() {
        testRestTemplate("/kryo", KryoHttpMessageConverter.APPLICATION_KRYO);
        testRestTemplate("/json", MediaType.APPLICATION_JSON);
    }

    @Test
    void testUrlConnection() throws Exception {
        testUrlConnection("/kryo", KryoHttpMessageConverter.APPLICATION_KRYO);
        testUrlConnection("/json", MediaType.APPLICATION_JSON);
    }

    @Test
    void testWebClient() {
        testWebClient("/kryo", AbstractKryoCodecSupport.APPLICATION_KRYO);
        testWebClient("/json", MediaType.APPLICATION_JSON);
    }

    @PostConstruct
    protected void setup() {
        final KryoHttpMessageConverter kryoHttpMessageConverter = new KryoHttpMessageConverter(KryoApplication.KRYO_POOL);

        restTemplate = new RestTemplateBuilder().rootUri("http://localhost:" + localServerPort)
                .additionalMessageConverters(kryoHttpMessageConverter, new MappingJackson2HttpMessageConverter()).build();

        // restTemplate = restTemplateBuilder.rootUri("http://localhost:" + localServerPort)
        // .additionalMessageConverters(kryoHttpMessageConverter).build();

        // Throws an UnsupportedMediaTypeException.

        // final ExchangeStrategies strategies = ExchangeStrategies.builder()
        //       .codecs(configurer -> {
        //           //configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
        //           configurer.customCodecs().register(new KryoEncoder(() -> KryoApplication.KRYO_SERIALIZER.get()));
        //           configurer.customCodecs().register(new KryoDecoder(() -> KryoApplication.KRYO_SERIALIZER.get()));
        //       }).build();

        webClientBuilder.baseUrl("http://localhost:" + localServerPort)
                //.exchangeStrategies(strategies) // Throws an UnsupportedMediaTypeException.
                .codecs(configurer -> {
                    // configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                    configurer.customCodecs().register(new KryoEncoder(KryoApplication.KRYO_POOL));
                    configurer.customCodecs().register(new KryoDecoder(KryoApplication.KRYO_POOL));
                })
        ;

        httpClientbuilder = HttpClient.newBuilder()
                .version(Version.HTTP_2)
                .executor(ForkJoinPool.commonPool())
        ;
    }

    protected void testHttpClient(final String path, final MimeType mimeType) throws Exception {
        try (HttpClient httpClient = httpClientbuilder.build()) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + localServerPort + path))
                    .header("Accept", mimeType.toString())
                    .GET()
                    .build();

            final HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());

            assertNotNull(response);
            assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith(mimeType.toString()));

            final HttpMessageConverterExtractor<LocalDateTime> converterExtractor =
                    new HttpMessageConverterExtractor<>(LocalDateTime.class, restTemplate.getMessageConverters());
            final MediaType mediaType = MediaType.asMediaType(mimeType);

            LocalDateTime localDateTime = null;

            try (ClientHttpResponse clientHttpResponse = new MockClientHttpResponse(response.body(), HttpStatus.OK)) {
                clientHttpResponse.getHeaders().setContentType(mediaType);

                localDateTime = converterExtractor.extractData(clientHttpResponse);
            }

            validateLocalDateTime(localDateTime);
        }
    }

    protected void testMockMvc(final String path, final MediaType mediaType) throws Exception {
        // MockMvcBuilders.standaloneSetup(controllers).
        final MockMvc mmvc = mockMvc;
        // final MockMvc mmvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // String url = "/test";
        final String url = "http://localhost:" + localServerPort + path;

        final AtomicReference<LocalDateTime> reference = new AtomicReference<>(null);

        mmvc.perform(get(url).accept(mediaType))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(mediaType)) //  + ";charset=UTF-8"
                //.andDo(print())
                .andDo(result -> {
                    final HttpMessageConverterExtractor<LocalDateTime> converterExtractor =
                            new HttpMessageConverterExtractor<>(LocalDateTime.class, restTemplate.getMessageConverters());
                    final byte[] bytes = result.getResponse().getContentAsByteArray();

                    LocalDateTime localDateTime = null;

                    try (ClientHttpResponse response = new MockClientHttpResponse(bytes, HttpStatus.OK)) {
                        response.getHeaders().setContentType(mediaType);

                        localDateTime = converterExtractor.extractData(response);
                    }

                    reference.set(localDateTime);
                })
        ;

        validateLocalDateTime(reference.get());
    }

    protected void testRestTemplate(final String path, final MediaType mediaType) {
        // final RestTemplateBuilder builder = new RestTemplateBuilder()
        //         .rootUri("http://localhost:" + localServerPort)
        //         .messageConverters(kryoHttpMessageConverter, new MappingJackson2HttpMessageConverter());
        //
        // RestTemplate restTemplate = builder.build();

        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(mediaType));

        final HttpEntity<String> entity = new HttpEntity<>(headers);

        final ResponseEntity<LocalDateTime> responseEntity = restTemplate.exchange(path, HttpMethod.GET, entity, LocalDateTime.class);

        assertTrue(mediaType.isCompatibleWith(responseEntity.getHeaders().getContentType()));

        final LocalDateTime localDateTime = responseEntity.getBody();

        validateLocalDateTime(localDateTime);
    }

    protected void testUrlConnection(final String path, final MediaType mediaType) throws Exception {
        final URI uri = URI.create("http://localhost:" + localServerPort + path);

        final HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();

        connection.setRequestMethod(HttpMethod.GET.toString());
        // connection.setRequestProperty("Content-Type", KryoHttpMessageConverter.APPLICATION_KRYO_VALUE);
        connection.setRequestProperty("Accept", mediaType.toString());
        connection.setDoOutput(false);
        connection.setDoInput(true);

        if (connection.getDoOutput()) {
            connection.setChunkedStreamingMode(4096);
        }

        connection.connect();

        assertTrue(connection.getHeaderField("Content-Type").startsWith(mediaType.toString()));

        final HttpMessageConverterExtractor<LocalDateTime> converterExtractor = new HttpMessageConverterExtractor<>(LocalDateTime.class, restTemplate.getMessageConverters());

        LocalDateTime localDateTime = null;

        try (ClientHttpResponse response = new MockClientHttpResponse(connection.getInputStream(), HttpStatus.OK)) {
            response.getHeaders().setContentType(mediaType);

            localDateTime = converterExtractor.extractData(response);
        }

        // if (KryoHttpMessageConverter.APPLICATION_KRYO.equals(mediaType)) {
        // localDateTime = (LocalDateTime) kryoHttpMessageConverter.read(LocalDateTime.class, new MockHttpInputMessage(connection.getInputStream()));
        // }
        // else {
        // localDateTime = objectMapper.readValue(connection.getInputStream(), LocalDateTime.class);
        // // LocalDateTime localDateTime = objectMapper.readValue(bytes, new TypeReference<LocalDateTime>() {
        // // });
        // }

        validateLocalDateTime(localDateTime);
    }

    protected void testWebClient(final String path, final MimeType mimeType) {
        final MediaType mediaType = MediaType.asMediaType(mimeType);

        final Mono<ResponseEntity<LocalDateTime>> response = webClientBuilder.build()
                .get()
                .uri(path)
                .accept(mediaType)
                .exchangeToMono(clientResponse -> clientResponse.toEntity(LocalDateTime.class)) // Returns Header and Status too.
                ;

        final ResponseEntity<LocalDateTime> responseEntity = response.block();

        assertNotNull(responseEntity);
        assertTrue(mediaType.isCompatibleWith(responseEntity.getHeaders().getContentType()));

        final LocalDateTime localDateTime = responseEntity.getBody();

        validateLocalDateTime(localDateTime);
    }
}

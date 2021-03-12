/**
 * Created: 22.05.2018
 */

package de.freese.spring.kryo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import de.freese.spring.kryo.web.KryoHttpMessageConverter;
import de.freese.spring.kryo.webflux.AbstractKryoCodecSupport;
import de.freese.spring.kryo.webflux.KryoDecoder;
import de.freese.spring.kryo.webflux.KryoEncoder;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = KryoApplication.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TestKryo
{
    /**
     * @param localDateTime {@link LocalDateTime}
     */
    static void validateLocalDateTime(final LocalDateTime localDateTime)
    {
        Assertions.assertNotNull(localDateTime);

        LocalDateTime now = LocalDateTime.now();

        Assertions.assertEquals(localDateTime.getYear(), now.getYear());
        Assertions.assertEquals(localDateTime.getMonth().getValue(), now.getMonth().getValue());
        Assertions.assertEquals(localDateTime.getDayOfMonth(), now.getDayOfMonth());
        Assertions.assertEquals(localDateTime.getHour(), now.getHour(), 1);
        Assertions.assertEquals(localDateTime.getMinute(), now.getMinute(), 1);
        // Assertions.assertEquals(localDateTime.getSecond(), now.getSecond());
    }

    /**
     *
     */
    private HttpClient.Builder httpClientbuilder = null;

    /**
     *
     */
    @LocalServerPort
    private int localServerPort = 0;

    /**
    *
    */
    @Resource
    private MockMvc mockMvc = null;

    // /**
    // *
    // */
    // @Resource
    // private RestTemplateBuilder restTemplateBuilder = null;

    /**
     *
     */
    @Resource
    private ObjectMapper objectMapper = null;

    /**
     *
     */
    private RestTemplate restTemplate = null;

    /**
     *
     */
    @Resource
    private WebApplicationContext webApplicationContext = null;

    /**
    *
    */
    @Resource
    private WebClient.Builder webClientBuilder = null;

    /**
     *
     */
    @PostConstruct
    protected void setup()
    {
        KryoHttpMessageConverter kryoHttpMessageConverter = new KryoHttpMessageConverter(KryoApplication.KRYO_POOL);

        this.restTemplate = new RestTemplateBuilder().rootUri("http://localhost:" + this.localServerPort)
                .additionalMessageConverters(kryoHttpMessageConverter, new MappingJackson2HttpMessageConverter()).build();

        // this.restTemplate = this.restTemplateBuilder.rootUri("http://localhost:" + this.localServerPort)
        // .additionalMessageConverters(this.kryoHttpMessageConverter).build();

        // @formatter:off
        // Verursacht eine UnsupportedMediaTypeException

//        ExchangeStrategies strategies = ExchangeStrategies.builder()
//              .codecs(configurer -> {
//                  //configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(this.objectMapper, MediaType.APPLICATION_JSON));
//                  configurer.customCodecs().register(new KryoEncoder(() -> KryoApplication.KRYO_SERIALIZER.get()));
//                  configurer.customCodecs().register(new KryoDecoder(() -> KryoApplication.KRYO_SERIALIZER.get()));
//              }).build();

        this.webClientBuilder.baseUrl("http://localhost:" + this.localServerPort)
            //.exchangeStrategies(strategies) // Verursacht eine UnsupportedMediaTypeException
            .codecs(configurer -> {
                //configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(this.objectMapper, MediaType.APPLICATION_JSON));
                configurer.customCodecs().register(new KryoEncoder( KryoApplication.KRYO_POOL));
                configurer.customCodecs().register(new KryoDecoder(KryoApplication.KRYO_POOL));
            })
            ;
        // @formatter:on

        // @formatter:off
        this.httpClientbuilder = HttpClient.newBuilder()
                .version(Version.HTTP_2)
                .executor(ForkJoinPool.commonPool())
                ;
        // @formatter:on
    }

    /**
     *
     */
    @Test
    void test010RestTemplate()
    {
        testRestTemplate("/kryo", KryoHttpMessageConverter.APPLICATION_KRYO);
        testRestTemplate("/json", MediaType.APPLICATION_JSON);
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void test020MockMvc() throws Exception
    {
        testMockMvc("/kryo", KryoHttpMessageConverter.APPLICATION_KRYO);
        testMockMvc("/json", MediaType.APPLICATION_JSON);
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void test030UrlConnection() throws Exception
    {
        testUrlConnection("/kryo", KryoHttpMessageConverter.APPLICATION_KRYO);
        testUrlConnection("/json", MediaType.APPLICATION_JSON);
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void test040WebClient() throws Exception
    {
        testWebClient("/kryo", AbstractKryoCodecSupport.APPLICATION_KRYO);
        testWebClient("/json", MediaType.APPLICATION_JSON);
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void test050HttpClient() throws Exception
    {
        testHttpClient("/kryo", AbstractKryoCodecSupport.APPLICATION_KRYO);
        testHttpClient("/json", MediaType.APPLICATION_JSON);
    }

    /**
     * @param path String
     * @param mimeType {@link MimeType}
     * @throws Exception Falls was schief geht.
     */
    protected void testHttpClient(final String path, final MimeType mimeType) throws Exception
    {
        HttpClient httpClient = this.httpClientbuilder.build();

        // @formatter:off
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + this.localServerPort+path))
                .header("Accept", mimeType.toString())
                .GET()
                .build();
                ;
        // @formatter:on

        HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());
        Assertions.assertTrue(response.headers().firstValue("Content-Type").get().startsWith(mimeType.toString()));

        HttpMessageConverterExtractor<LocalDateTime> converterExtractor =
                new HttpMessageConverterExtractor<>(LocalDateTime.class, this.restTemplate.getMessageConverters());
        MediaType mediaType = MediaType.asMediaType(mimeType);

        LocalDateTime localDateTime = null;

        try (ClientHttpResponse clientHttpResponse = new MockClientHttpResponse(response.body(), HttpStatus.OK))
        {
            clientHttpResponse.getHeaders().setContentType(mediaType);

            localDateTime = converterExtractor.extractData(clientHttpResponse);
        }

        validateLocalDateTime(localDateTime);
    }

    /**
     * @param path String
     * @param mediaType {@link MediaType}
     * @throws Exception Falls was schief geht.
     */
    protected void testMockMvc(final String path, final MediaType mediaType) throws Exception
    {
        // MockMvcBuilders.standaloneSetup(controllers).
        MockMvc mmvc = this.mockMvc;
        // MockMvc mmvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        // String url = "/test";
        String url = "http://localhost:" + this.localServerPort + path;

        AtomicReference<LocalDateTime> reference = new AtomicReference<>(null);

        // @formatter:off
        mmvc.perform(get(url).accept(mediaType))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(mediaType)) //  + ";charset=UTF-8"
           //.andDo(print())
           .andDo(result -> {
               HttpMessageConverterExtractor<LocalDateTime> converterExtractor =
                       new HttpMessageConverterExtractor<>(LocalDateTime.class, this.restTemplate.getMessageConverters());
               byte[] bytes = result.getResponse().getContentAsByteArray();

               LocalDateTime localDateTime = null;

               try (ClientHttpResponse response = new MockClientHttpResponse(bytes, HttpStatus.OK))
               {
                   response.getHeaders().setContentType(mediaType);

                   localDateTime = converterExtractor.extractData(response);
               }

               reference.set(localDateTime);
           })
           ;
        // @formatter:on

        validateLocalDateTime(reference.get());
    }

    /**
     * @param path String
     * @param mediaType {@link MediaType}
     */
    protected void testRestTemplate(final String path, final MediaType mediaType)
    {
//        // @formatter:off
//        RestTemplateBuilder builder = new RestTemplateBuilder()
//                .rootUri("http://localhost:" + this.localServerPort)
//                .messageConverters(this.kryoHttpMessageConverter,
//                            new MappingJackson2HttpMessageConverter());
//        // @formatter:on
        //
        // RestTemplate restTemplate = builder.build();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(mediaType));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<LocalDateTime> responseEntity = this.restTemplate.exchange(path, HttpMethod.GET, entity, LocalDateTime.class);

        Assertions.assertTrue(mediaType.isCompatibleWith(responseEntity.getHeaders().getContentType()));

        LocalDateTime localDateTime = responseEntity.getBody();

        validateLocalDateTime(localDateTime);
    }

    /**
     * @param path String
     * @param mediaType {@link MediaType}
     * @throws Exception Falls was schief geht.
     */
    protected void testUrlConnection(final String path, final MediaType mediaType) throws Exception
    {
        URL url = new URL("http", "localhost", this.localServerPort, path);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(HttpMethod.GET.toString());
        // connection.setRequestProperty("Content-Type", KryoHttpMessageConverter.APPLICATION_KRYO_VALUE);
        connection.setRequestProperty("Accept", mediaType.toString());
        connection.setDoOutput(false);
        connection.setDoInput(true);

        if (connection.getDoOutput())
        {
            connection.setChunkedStreamingMode(4096);
        }

        connection.connect();

        Assertions.assertTrue(connection.getHeaderField("Content-Type").startsWith(mediaType.toString()));

        HttpMessageConverterExtractor<LocalDateTime> converterExtractor =
                new HttpMessageConverterExtractor<>(LocalDateTime.class, this.restTemplate.getMessageConverters());

        LocalDateTime localDateTime = null;

        try (ClientHttpResponse response = new MockClientHttpResponse(connection.getInputStream(), HttpStatus.OK))
        {
            response.getHeaders().setContentType(mediaType);

            localDateTime = converterExtractor.extractData(response);
        }

        // if (KryoHttpMessageConverter.APPLICATION_KRYO.equals(mediaType))
        // {
        // localDateTime = (LocalDateTime) this.kryoHttpMessageConverter.read(LocalDateTime.class, new MockHttpInputMessage(connection.getInputStream()));
        // }
        // else
        // {
        // localDateTime = this.objectMapper.readValue(connection.getInputStream(), LocalDateTime.class);
        // // LocalDateTime localDateTime = this.objectMapper.readValue(bytes, new TypeReference<LocalDateTime>()
        // // {
        // // });
        // }

        validateLocalDateTime(localDateTime);
    }

    /**
     * @param path String
     * @param mimeType {@link MimeType}
     */
    protected void testWebClient(final String path, final MimeType mimeType)
    {
        MediaType mediaType = MediaType.asMediaType(mimeType);

        // @formatter:off
        Mono<ResponseEntity<LocalDateTime>> response = this.webClientBuilder.build()
                .get()
                .uri(path)
                .accept(mediaType)
                .exchangeToMono(clientResponse -> clientResponse.toEntity(LocalDateTime.class)) // Liefert auch Header und Status
                ;
        // @formatter:on

        ResponseEntity<LocalDateTime> responseEntity = response.block();

        Assertions.assertTrue(mediaType.isCompatibleWith(responseEntity.getHeaders().getContentType()));

        LocalDateTime localDateTime = responseEntity.getBody();

        validateLocalDateTime(localDateTime);
    }
}

// Erzeugt: 04.05.2016
package de.freese.spring.hateoas;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Resource;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.freese.spring.hateoas.model.GreetingPOJO;
import de.freese.spring.hateoas.model.GreetingResourceSupport;

/**
 * @author Thomas Freese
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HateoasApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @DirtiesContext
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestHateoas
{
    /**
     * @throws Exception Falls was schief geht.
     */
    @BeforeClass
    public static void setUp() throws Exception
    {
        // Logger logger = (Logger) LoggerFactory.getLogger("ROOT");
        // logger.setLevel(Level.ERROR);
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @AfterClass
    public static void shutdown() throws Exception
    {
        // PRINT_STREAM.println("JAVA_ENV: " + System.getProperty("JAVA_ENV"));
        // PRINT_STREAM.println("JAVA_ENV: " + System.getenv("JAVA_ENV"));
    }

    /**
     *
     */
    @Value("${server.servlet.context-path:}")
    private String contextPath = null;

    /**
     *
     */
    @Resource
    private ObjectMapper objectMapper = null;

    /**
     *
     */
    // @Value("${local.server.port}") // aus application.properties für WebEnvironment.DEFINED_PORT
    @LocalServerPort // WebEnvironment.RANDOM_PORT
    private int port = -1;

    /**
     * Wird für {@link MockMvc} benötigt.
     */
    @Resource
    private WebApplicationContext wac = null;

    /**
     * Erzeugt eine neue Instanz von {@link TestHateoas}
     */
    public TestHateoas()
    {
        super();
    }

    /**
     * @return {@link URI}
     * @throws URISyntaxException Falls was schief geht.
     */
    private URI getBaseURI() throws URISyntaxException
    {
        URI uri = new URI("http://localhost:" + this.port + this.contextPath + "/greeter/");
        // System.out.println(uri);

        return uri;
    }

    /**
     * Ergebnis: {"_links":{"self":{"href":"http://localhost:9000/hateoas/greeter/?name=World"}},"greeting":"Hello, World!"}
     *
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void greeting() throws Exception
    {
        Traverson traverson = new Traverson(getBaseURI(), MediaTypes.HAL_JSON);
        String greeting = traverson.follow("self").toObject("$.greeting");
        Assert.assertEquals("Hello, World!", greeting);
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void greetingFail() throws Exception
    {
        RestTemplate restTemplate = new RestTemplate(Arrays.asList(new StringHttpMessageConverter(StandardCharsets.UTF_8)));
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler()
        {
            /**
             * @see org.springframework.web.client.DefaultResponseErrorHandler#handleError(org.springframework.http.client.ClientHttpResponse)
             */
            @Override
            public void handleError(final ClientHttpResponse response) throws IOException
            {
                // HttpStatus statusCode = getHttpStatusCode(response);
                // String statusText = response.getStatusText();
                // HttpHeaders httpHeaders = response.getHeaders();
                // byte[] body = getResponseBody(response);
                // Charset charset = getCharset(response);
                //
                // String bodyString = new String(body, charset);
                // bodyString = bodyString.replace("\\r\\n", System.lineSeparator());
                // bodyString = bodyString.replace("\\t", "\t");

                // System.out.println(bodyString);
                super.handleError(response);
            }
            // /**
            // * ErrorHandler anpassen, wenn keine Standard HTTP-Stati verwendet werden.
            // *
            // * @see
            // org.springframework.web.client.DefaultResponseErrorHandler#hasError(org.springframework.http.client.ClientHttpResponse)
            // */
            // @Override
            // public boolean hasError(final ClientHttpResponse response) throws IOException
            // {
            // // try
            // // {
            // return hasError(getHttpStatusCode(response));
            // // }
            // // catch (UnknownHttpStatusCodeException ex)
            // // {
            // // // Default: false
            // // return true;
            // // }
            // }
        });

        try
        {
            restTemplate.getForObject(getBaseURI().resolve("fail"), String.class);

            Assert.assertTrue(false);
        }
        catch (RestClientResponseException ex)
        {
            Assert.assertTrue(true);
            Assert.assertTrue(HttpStatus.BAD_REQUEST.equals(((HttpStatusCodeException) ex).getStatusCode()));
            // Assert.assertTrue(999 == ex.getRawStatusCode());

            // GreetingException gex = this.objectMapper.readValue(ex.getResponseBodyAsString(), GreetingException.class);
            // gex.printStackTrace();
            //
            // System.out.println(ex.getResponseBodyAsString());
            // System.out.println(ex.getResponseBodyAsString().replace("\\r\\n", System.lineSeparator()).replace("\\t", "\t"));
        }
    }

    /**
     * Ergebnis: {"_links":{"self":{"href":"http://localhost:9000/hateoas/greeter/?name=World"}},"greeting":"Hello, World!"}
     *
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void greetingGetLinks() throws Exception
    {
        // JSON für die Links im Response konfigurieren.
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(new Jackson2HalModule());

        MappingJackson2HttpMessageConverter halConverter = new MappingJackson2HttpMessageConverter();
        // MappingJackson2HttpMessageConverter halConverter = new TypeConstrainedMappingJackson2HttpMessageConverter(ResourceSupport.class);
        halConverter.setSupportedMediaTypes(Arrays.asList(MediaTypes.HAL_JSON, MediaType.APPLICATION_JSON_UTF8));
        // halConverter.setPrettyPrint(true);
        halConverter.setObjectMapper(mapper);

        // RestTemplate restTemplate = new RestTemplate();
        // restTemplate.getMessageConverters().add(0, halConverter); // Neuer muss VOR dem alten MappingJackson2HttpMessageConverter stehen.
        // Default Converter überschreiben.
        RestTemplate restTemplate = new RestTemplate(Arrays.asList(halConverter));

        URI uri = getBaseURI();

        ResponseEntity<GreetingResourceSupport> greeting = restTemplate.getForEntity(uri, GreetingResourceSupport.class);
        // ResponseEntity<GreetingResourceSupport> greeting = restTemplate.exchange(uri, HttpMethod.GET, HttpEntity.EMPTY,
        // GreetingResourceSupport.class);
        // ResponseEntity<org.springframework.hateoas.Resource<GreetingResourceSupport>> greeting = restTemplate.exchange(uri,
        // HttpMethod.GET,
        // null, new ParameterizedTypeReference<org.springframework.hateoas.Resource<GreetingResourceSupport>>()
        // {
        // });
        MediaType mediaType = greeting.getHeaders().getContentType();
        HttpStatus statusCode = greeting.getStatusCode();

        Assert.assertNotNull(greeting);
        Assert.assertNotNull(greeting.getBody());
        Assert.assertTrue(MediaTypes.HAL_JSON.isCompatibleWith(mediaType));
        Assert.assertTrue(HttpStatus.OK.equals(statusCode));

        Assert.assertEquals("Hello, World!", greeting.getBody().getMessage());

        List<Link> links = greeting.getBody().getLinks();
        Assert.assertNotNull(links);
        Assert.assertTrue(links.size() > 1);

        Link link = greeting.getBody().getLink("forPath");
        Assert.assertNotNull(link);
        Assert.assertEquals(uri.resolve("path/World").toString(), link.getHref());
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void greetingJsonRaw() throws Exception
    {
        RestTemplate restTemplate = new RestTemplate(Arrays.asList(new StringHttpMessageConverter(StandardCharsets.UTF_8)));

        String jsonRaw = restTemplate.getForObject(getBaseURI(), String.class);

        Assert.assertNotNull(jsonRaw);
        System.out.println(jsonRaw);
    }

    /**
     * Ergebnis: {"greeting":"Hello, test!","_links":{"self":{"href":"http://localhost:9000/hateoas/greeter/path/test"}}}
     *
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void greetingPATH() throws Exception
    {
        Traverson traverson = new Traverson(getBaseURI().resolve("path/test"), MediaTypes.HAL_JSON);
        String greeting = traverson.follow("self").toObject("$.greeting");
        Assert.assertEquals("Hello, test!", greeting);
    }

    /**
     * Ergebnis: {"greeting":"Hello, World!","_links":{"self":{"href":"http://localhost:9000/hateoas/greeter/pojo?name=World"}}}
     *
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void greetingPOJO() throws Exception
    {
        Traverson traverson = new Traverson(getBaseURI().resolve("pojo"), MediaTypes.HAL_JSON);
        String greeting = traverson.follow("self").toObject("$.greeting");
        Assert.assertEquals("Hello, World!", greeting);
    }

    /**
     * Ergebnis: {"greeting":"Hello, World!"}
     *
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void greetingSimple() throws Exception
    {
        RestTemplate restTemplate = new RestTemplate();
        GreetingPOJO pojo = restTemplate.getForObject(getBaseURI().resolve("simple"), GreetingPOJO.class);
        Assert.assertNotNull(pojo);
        Assert.assertNotNull(pojo.getMessage());
        Assert.assertEquals("Hello, World!", pojo.getMessage());

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        // @formatter:off
        mockMvc.perform(MockMvcRequestBuilders.get("/greeter/simple"))
                //.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.greeting").value("Hello, World!"));

        mockMvc.perform(MockMvcRequestBuilders.get("/greeter/simple").param("name", "Test"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.greeting").value("Hello, Test!"));
        // @formatter:on
    }
}

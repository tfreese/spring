/**
 * Created: 22.05.2018
 */

package de.freese.spring.kryo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Calendar;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Thomas Freese
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes =
{
        KryoApplication.class
}, properties = {})
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestKryo
{
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

    /**
     *
     */
    @Resource
    private ObjectMapper objectMapper = null;

    /**
     *
     */
    @Resource
    private WebApplicationContext webApplicationContext = null;

    /**
     * Erstellt ein neues {@link TestKryo} Object.
     */
    public TestKryo()
    {
        super();
    }

    /**
     * @param path String
     * @param mediaType {@link MediaType}
     * @throws Exception Falls was schief geht.
     */
    void mockMvc(final String path, final MediaType mediaType) throws Exception
    {
        // MockMvcBuilders.standaloneSetup(controllers).
        MockMvc mmvc = this.mockMvc;
        // MockMvc mmvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        // String url = "/test";
        String url = "http://localhost:" + this.localServerPort + path;

       // @formatter:off
        mmvc.perform(get(url).accept(mediaType))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(mediaType)); //  + ";charset=UTF-8"
       // @formatter:on
    }

    /**
     * @param path String
     * @param mediaType {@link MediaType}
     */
    void restTemplate(final String path, final MediaType mediaType)
    {
        // @formatter:off
        RestTemplateBuilder builder = new RestTemplateBuilder()
                .rootUri("http://localhost:" + this.localServerPort)
                .messageConverters(new KryoHttpMessageConverter(() -> KryoApplication.KRYO_SERIALIZER.get()),
                            new MappingJackson2HttpMessageConverter());
        // @formatter:on

        RestTemplate restTemplate = builder.build();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(mediaType));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<LocalDateTime> response = restTemplate.exchange(path, HttpMethod.GET, entity, LocalDateTime.class);
        LocalDateTime resource = response.getBody();

        Assert.assertNotNull(resource);

        Calendar actual = Calendar.getInstance();
        Assert.assertEquals(resource.getYear(), actual.get(Calendar.YEAR));
        Assert.assertEquals(resource.getMonth().getValue(), actual.get(Calendar.MONTH) + 1);
        Assert.assertEquals(resource.getDayOfMonth(), actual.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(resource.getHour(), actual.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(resource.getMinute(), actual.get(Calendar.MINUTE));
        // Assert.assertEquals(resource.getSecond(), actual.get(Calendar.SECOND));
    }

    /**
     *
     */
    @Test
    public void test010RestTemplate()
    {
        restTemplate("/kryo", KryoHttpMessageConverter.APPLICATION_KRYO);
        restTemplate("/json", MediaType.APPLICATION_JSON);
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test020MockMvc() throws Exception
    {
        mockMvc("/kryo", KryoHttpMessageConverter.APPLICATION_KRYO);
        mockMvc("/json", MediaType.APPLICATION_JSON);
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test030UrlConnection() throws Exception
    {
        urlConnection("/kryo", KryoHttpMessageConverter.APPLICATION_KRYO);
        urlConnection("/json", MediaType.APPLICATION_JSON);
    }

    /**
     * @param path String
     * @param mediaType {@link MediaType}
     * @throws Exception Falls was schief geht.
     */
    void urlConnection(final String path, final MediaType mediaType) throws Exception
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

        LocalDateTime resource = null;

        if (KryoHttpMessageConverter.APPLICATION_KRYO.equals(mediaType))
        {
            try (Input input = new Input(connection.getInputStream(), 4096))
            {
                Kryo kryo = KryoApplication.KRYO_SERIALIZER.get();

                resource = (LocalDateTime) kryo.readClassAndObject(input);
            }
        }
        else
        {
            resource = this.objectMapper.readValue(connection.getInputStream(), LocalDateTime.class);
        }

        Assert.assertNotNull(resource);

        Calendar actual = Calendar.getInstance();
        Assert.assertEquals(resource.getYear(), actual.get(Calendar.YEAR));
        Assert.assertEquals(resource.getMonth().getValue(), actual.get(Calendar.MONTH) + 1);
        Assert.assertEquals(resource.getDayOfMonth(), actual.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(resource.getHour(), actual.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(resource.getMinute(), actual.get(Calendar.MINUTE));
        // Assert.assertEquals(resource.getSecond(), actual.get(Calendar.SECOND));
    }
}

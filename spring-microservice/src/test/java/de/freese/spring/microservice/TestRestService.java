// Created: 14.02.2017
package de.freese.spring.microservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.freese.spring.Server;
import de.freese.spring.microservice.RestService.Clock;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Thomas Freese
 */
@Configuration
class Config
{
    /**
     * Erzeugt eine neue Instanz von {@link Config}
     */
    Config()
    {
        super();
    }

    // /**
    // * JSON-Mapper
    // *
    // * @param builder {@link Jackson2ObjectMapperBuilder}
    // * @return {@link ObjectMapper}
    // */
    // @Bean
    // @Primary
    // public ObjectMapper objectMapperJSON(final Jackson2ObjectMapperBuilder builder)
    // {
    // ObjectMapper objectMapper = builder.createXmlMapper(false).build();
    //
    // return objectMapper;
    // }

    // /**
    // * XML-Mapper
    // *
    // * @param builder {@link Jackson2ObjectMapperBuilder}
    // * @return {@link ObjectMapper}
    // */
    // @Bean
    // public ObjectMapper objectMapperXML(final Jackson2ObjectMapperBuilder builder)
    // {
    // ObjectMapper objectMapper = builder.createXmlMapper(true).build();
    //
    // return objectMapper;
    // }
}

/**
 * @author Thomas Freese
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes =
        {
                Server.class
        }, properties = {})
@Import(Config.class)
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
// @Ignore
public class TestRestService
{
    // @SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes =
    // {
    // Server.class
    // }, properties =
    // {
    // "server.port=65500"
    // })
    // @DirtiesContext

    /**
     *
     */
    @Resource
    private MockMvc mockMvc = null;

    /**
     * Default für JSON.
     */
    @Resource
    private ObjectMapper objectMapper = null;
    //
    // /**
    // * Default für XML.
    // */
    // @Resource
    // // @Qualifier("objectMapperXML")
    // private ObjectMapper objectMapperXML = null;

    /**
     * Für XML-Mapping
     */
    @Resource
    private Jackson2ObjectMapperBuilder objectMapperBuilder = null;

    // /**
    // *
    // */
    // @Resource
    // private WebTestClient webClient = nullt;

    /**
     * Erzeugt eine neue Instanz von {@link TestRestService}
     */
    public TestRestService()
    {
        super();
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test010Ping() throws Exception
    {
        // .andDo(print()).andExpect(jsonPath("$.content").value("Hello, Spring Community!"));

        // @formatter:off
        this.mockMvc.perform(get("/service/ping")) // Test-URLs ohne Context-Root angeben.
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
//          .andDo(MockMvcResultHandlers.print())
            .andExpect(content().string("true"))
            .andExpect(MockMvcResultMatchers.jsonPath("$").value("true")) // Alternative zu string("true")
            .andExpect(MockMvcResultMatchers.jsonPath("$").value(Matchers.is(true))); // Alternative zu string("true")

//        this.webClient.get().repository("/")
//            .exchange()
//            .expectStatus().isOk()
//            .exp
//            .expectBody(String.class).isEqualTo("true");
        // @formatter:on
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test020Sysdate() throws Exception
    {
        // @formatter:off
        this.mockMvc.perform(get("/service/sysdate")) // Test-URLs ohne Context-Root angeben.
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            //.andExpect(content().string(CoreMatchers.containsString("at port")))
            .andExpect(MockMvcResultMatchers.jsonPath("$").exists())
            //.andExpect(MockMvcResultMatchers.jsonPath("$").value(Matchers.containsString("at port"))) // Alternative zu string(CoreMatchers.containsString("at port"))
        ;
        // @formatter:on
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test030ClockJSON() throws Exception
    {
        AtomicReference<Clock> clockReference = new AtomicReference<>(null);

        // @formatter:off
        this.mockMvc.perform(
                            get("/service/clock") // Test-URLs ohne Context-Root angeben
                            .accept(MediaType.APPLICATION_JSON_UTF8)
                            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.jsonPath("$.date").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.local_date").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.local_time").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.local_date_time").exists())
            .andDo(result -> {
                Clock clock = this.objectMapper.readValue(result.getResponse().getContentAsByteArray(), Clock.class);
                clockReference.set(clock);
            });
        // @formatter:on

        Clock clock = clockReference.get();
        Assert.assertNotNull(clock);
        Assert.assertNotNull(clock.getDate());
        Assert.assertNotNull(clock.getLocalDate());
        Assert.assertNotNull(clock.getLocalTime());
        Assert.assertNotNull(clock.getLocalDateTime());
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test031ClockXML() throws Exception
    {
        ObjectMapper objectMapperXML = this.objectMapperBuilder.createXmlMapper(true).build();
        AtomicReference<Clock> clockReference = new AtomicReference<>(null);

        // @formatter:off
        this.mockMvc.perform(
                get("/service/clock") // Test-URLs ohne Context-Root angeben
                .accept(MediaType.APPLICATION_XML)
                )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/xml;charset=UTF-8")) // "application/xml;charset=UTF-8"
            .andExpect(MockMvcResultMatchers.xpath("Clock/date").exists())
            .andExpect(MockMvcResultMatchers.xpath("Clock/local_date").exists())
            .andExpect(MockMvcResultMatchers.xpath("Clock/local_time").exists())
            .andExpect(MockMvcResultMatchers.xpath("Clock/local_date_time").exists())
            .andDo(result -> {
                Clock clock = objectMapperXML.readValue(result.getResponse().getContentAsByteArray(), Clock.class);
                clockReference.set(clock);
            });
        // @formatter:on

        Clock clock = clockReference.get();
        Assert.assertNotNull(clock);
        Assert.assertNotNull(clock.getDate());
        Assert.assertNotNull(clock.getLocalDate());
        Assert.assertNotNull(clock.getLocalTime());
        Assert.assertNotNull(clock.getLocalDateTime());
    }
}

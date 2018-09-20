/**
 * Created: 07.09.2018
 */

package de.freese.spring.thymeleaf.rest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.freese.spring.thymeleaf.ThymeleafApplication;
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
// @ActiveProfiles(
// {
// "test", "with-ssl"
// })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestRestWithMockMvc implements RestTestCase
{
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

    /**
     * Für XML-Mapping
     */
    @Resource
    private Jackson2ObjectMapperBuilder objectMapperBuilder = null;

    /**
     * Erstellt ein neues {@link TestRestWithMockMvc} Object.
     */
    public TestRestWithMockMvc()
    {
        super();
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test000HealthEndpoint()
     */
    @Override
    @Test
    public void test000HealthEndpoint() throws Exception
    {
        // @formatter:off
        this.mockMvc.perform(get("/actuator/health")
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").exists())
            .andExpect(jsonPath("$.status").value("UP"));
        // @formatter:on
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test010UserWithoutLogin()
     */
    @Override
    @Test
    public void test010UserWithoutLogin() throws Exception
    {
        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList"))
            .andExpect(status().isUnauthorized());
        // @formatter:on
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test011UserWithWrongPass()
     */
    @Override
    @Test
    public void test011UserWithWrongPass() throws Exception
    {
        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .with(httpBasic("user", "pass")))
            .andExpect(status().isUnauthorized());
        // @formatter:on
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test020UserWithWrongRole()
     */
    @Override
    @Test
    // @WithMockUser(username = "invalid", password = "pw", roles = "OTHER")
    public void test020UserWithWrongRole() throws Exception
    {
        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .with(httpBasic("invalid", "pw")))
            .andExpect(status().isForbidden());
        // @formatter:on
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test030UserWithLoginJSON()
     */
    @Override
    @Test
    // @WithMockUser(username = "user", password = "pw")
    public void test030UserWithLoginJSON() throws Exception
    {
        AtomicReference<List<Person>> reference = new AtomicReference<>(null);

        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .with(httpBasic("user", "pw"))
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andDo(result -> {
                List<Person> list = this.objectMapper.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<List<Person>>()
                {
                });
                reference.set(list);
            });
        // @formatter:on

        List<Person> persons = reference.get();
        Assert.assertNotNull(persons);
        Assert.assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test031UserWithLoginXML()
     */
    @Override
    @Test
    // @WithMockUser(username = "user", password = "pw")
    public void test031UserWithLoginXML() throws Exception
    {
        ObjectMapper objectMapperXML = this.objectMapperBuilder.createXmlMapper(true).build();
        AtomicReference<List<Person>> reference = new AtomicReference<>(null);

        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .with(httpBasic("admin", "pw"))
                .accept(MediaType.APPLICATION_XML))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8"))
            .andDo(result -> {
                List<Person> list = objectMapperXML.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<List<Person>>()
                {
                });
                reference.set(list);
            });
        // @formatter:on

        List<Person> persons = reference.get();
        Assert.assertNotNull(persons);
        Assert.assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test040PostWithWrongRole()
     */
    @Override
    @Test
    // @WithMockUser(username = "user", password = "pw")
    public void test040PostWithWrongRole() throws Exception
    {
        // @formatter:off
        this.mockMvc.perform(post("/rest/person/personAdd")
                .with(httpBasic("user", "pw"))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"firstName\":\"Thomas\",\"lastName\":\"Freese\"}"))
            .andExpect(status().isForbidden());
        // @formatter:on
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.RestTestCase#test041Post()
     */
    @Override
    @Test
    // @WithMockUser(username = "admin", password = "pw", roles = "ADMIN")
    public void test041Post() throws Exception
    {
        // @formatter:off
        this.mockMvc.perform(post("/rest/person/personAdd")
                    .with(httpBasic("admin", "pw"))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"firstName\":\"Thomas\", \"lastName\":\"Freese\"}"))
            .andDo(print())
            .andExpect(status().isOk());
        // @formatter:on

        AtomicReference<List<Person>> reference = new AtomicReference<>(null);

        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .with(httpBasic("user", "pw"))
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andDo(result -> {
                List<Person> list = this.objectMapper.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<List<Person>>()
                {
                });
                reference.set(list);
            });
        // @formatter:on

        List<Person> persons = reference.get();
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
        AtomicReference<List<Person>> reference = new AtomicReference<>(null);

        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .header("my-token", "user")
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andDo(result -> {
                List<Person> list = this.objectMapper.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<List<Person>>()
                {
                });
                reference.set(list);
            });
        // @formatter:on

        List<Person> persons = reference.get();
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
        ObjectMapper objectMapperXML = this.objectMapperBuilder.createXmlMapper(true).build();
        AtomicReference<List<Person>> reference = new AtomicReference<>(null);

        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .header("my-token", "admin")
                .accept(MediaType.APPLICATION_XML))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_XML_VALUE+";charset=UTF-8"))
            .andDo(result -> {
                List<Person> list = objectMapperXML.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<List<Person>>()
                {
                });
                reference.set(list);
            });
        // @formatter:on

        List<Person> persons = reference.get();
        Assert.assertNotNull(persons);
        Assert.assertTrue(persons.size() >= 2);
    }
}

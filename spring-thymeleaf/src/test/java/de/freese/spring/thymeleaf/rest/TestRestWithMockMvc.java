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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.freese.spring.thymeleaf.ThymeleafApplication;
import de.freese.spring.thymeleaf.model.Person;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = ThymeleafApplication.class)
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
// @ActiveProfiles(
// {
// "test", "with-ssl"
// })
public class TestRestWithMockMvc extends AbstractRestTestCase
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
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test000HealthEndpoint()
     */
    @Override
    @Test
    void test000HealthEndpoint() throws Exception
    {
        // @formatter:off
        this.mockMvc.perform(get("/actuator/health")
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").exists())
            .andExpect(jsonPath("$.status").value("UP"));
        // @formatter:on
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test010UserWithoutLogin()
     */
    @Override
    @Test
    void test010UserWithoutLogin() throws Exception
    {
        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList"))
            .andExpect(status().isUnauthorized());
        // @formatter:on
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test011UserWithWrongPass()
     */
    @Override
    @Test
    void test011UserWithWrongPass() throws Exception
    {
        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .with(httpBasic("user", "pass")))
            .andExpect(status().isUnauthorized());
        // @formatter:on
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test020UserWithWrongRole()
     */
    @Override
    @Test
    // @WithMockUser(username = "invalid", password = "pw", roles = "OTHER")
    void test020UserWithWrongRole() throws Exception
    {
        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .with(httpBasic("invalid", "pw")))
            .andExpect(status().isForbidden());
        // @formatter:on
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test030UserWithLoginJSON()
     */
    @Override
    @Test
    // @WithMockUser(username = "user", password = "pw")
    void test030UserWithLoginJSON() throws Exception
    {
        AtomicReference<List<Person>> reference = new AtomicReference<>(null);

        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .with(httpBasic("user", "pw"))
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(result -> {
                List<Person> list = this.objectMapper.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<List<Person>>()
                {
                });
                reference.set(list);
            });
        // @formatter:on

        List<Person> persons = reference.get();
        Assertions.assertNotNull(persons);
        Assertions.assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test031UserWithLoginXML()
     */
    @Override
    @Test
    // @WithMockUser(username = "user", password = "pw")
    void test031UserWithLoginXML() throws Exception
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
        Assertions.assertNotNull(persons);
        Assertions.assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test040PostWithWrongRole()
     */
    @Override
    @Test
    // @WithMockUser(username = "user", password = "pw")
    void test040PostWithWrongRole() throws Exception
    {
        // @formatter:off
        this.mockMvc.perform(post("/rest/person/personAdd")
                .with(httpBasic("user", "pw"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Thomas\",\"lastName\":\"Freese\"}"))
            .andExpect(status().isForbidden());
        // @formatter:on
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test041Post()
     */
    @Override
    @Test
    // @WithMockUser(username = "admin", password = "pw", roles = "ADMIN")
    void test041Post() throws Exception
    {
        // @formatter:off
        this.mockMvc.perform(post("/rest/person/personAdd")
                    .with(httpBasic("admin", "pw"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Thomas\", \"lastName\":\"Freese\"}"))
            .andDo(print())
            .andExpect(status().isOk());
        // @formatter:on

        AtomicReference<List<Person>> reference = new AtomicReference<>(null);

        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .with(httpBasic("user", "pw"))
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(result -> {
                List<Person> list = this.objectMapper.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<List<Person>>()
                {
                });
                reference.set(list);
            });
        // @formatter:on

        List<Person> persons = reference.get();
        Assertions.assertNotNull(persons);
        Assertions.assertTrue(persons.size() >= 3);

        Assertions.assertEquals("Thomas", persons.get(persons.size() - 1).getFirstName());
        Assertions.assertEquals("Freese", persons.get(persons.size() - 1).getLastName());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test050UserWithPreAuthJSON()
     */
    @Override
    @Test
    void test050UserWithPreAuthJSON() throws Exception
    {
        AtomicReference<List<Person>> reference = new AtomicReference<>(null);

        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .header("my-token", "user")
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(result -> {
                List<Person> list = this.objectMapper.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<List<Person>>()
                {
                });
                reference.set(list);
            });
        // @formatter:on

        List<Person> persons = reference.get();
        Assertions.assertNotNull(persons);
        Assertions.assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#test051UserWithPreAuthXML()
     */
    @Override
    @Test
    void test051UserWithPreAuthXML() throws Exception
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
        Assertions.assertNotNull(persons);
        Assertions.assertTrue(persons.size() >= 2);
    }
}

// Created: 07.09.2018
package de.freese.spring.thymeleaf.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.annotation.Resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import de.freese.spring.thymeleaf.model.Person;

/**
 * @author Thomas Freese
 */
@ActiveProfiles("test")
        // @ActiveProfiles(
        // {
        // "test", "with-ssl"
        // })
class TestRestWithMockMvc extends AbstractRestTestCase {
    @Resource
    private MockMvc mockMvc;

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#testHealthEndpoint()
     */
    @Override
    @Test
    void testHealthEndpoint() throws Exception {
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
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#testPost()
     */
    @Override
    @Test
    // @WithMockUser(username = "admin", password = "pw", roles = "ADMIN")
    void testPost() throws Exception {
        // @formatter:off
        this.mockMvc.perform(post("/rest/person/personAdd")
                .with(httpBasic("admin", "pw"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Thomas\", \"lastName\":\"Freese\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                ;
        // @formatter:on

        AtomicReference<List<Person>> reference = new AtomicReference<>(null);

        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .with(httpBasic("user", "pw"))
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(result -> {
                    List<Person> list = getObjectMapper().readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>()
                    {
                    });
                    reference.set(list);
                })
                ;
        // @formatter:on

        List<Person> persons = reference.get();
        assertNotNull(persons);
        assertTrue(persons.size() >= 3);

        assertEquals("Thomas", persons.get(persons.size() - 1).getFirstName());
        assertEquals("Freese", persons.get(persons.size() - 1).getLastName());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#testPostWithWrongRole()
     */
    @Override
    @Test
    // @WithMockUser(username = "user", password = "pw")
    void testPostWithWrongRole() throws Exception {
        // @formatter:off
        this.mockMvc.perform(post("/rest/person/personAdd")
                .with(httpBasic("user", "pw"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Thomas\",\"lastName\":\"Freese\"}"))
                .andExpect(status().isForbidden())
                ;
        // @formatter:on
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#testUserWithLoginJSON()
     */
    @Override
    @Test
    // @WithMockUser(username = "user", password = "pw")
    void testUserWithLoginJSON() throws Exception {
        AtomicReference<List<Person>> reference = new AtomicReference<>(null);

        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .with(httpBasic("user", "pw"))
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(result -> {
                    List<Person> list = getObjectMapper().readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>()
                    {
                    });
                    reference.set(list);
                })
                ;
        // @formatter:on

        List<Person> persons = reference.get();
        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#testUserWithLoginXML()
     */
    @Override
    @Test
    // @WithMockUser(username = "user", password = "pw")
    void testUserWithLoginXML() throws Exception {
        ObjectMapper objectMapperXML = getObjectMapperBuilder().createXmlMapper(true).build();
        AtomicReference<List<Person>> reference = new AtomicReference<>(null);

        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .with(httpBasic("admin", "pw"))
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8"))
                .andDo(result -> {
                    List<Person> list = objectMapperXML.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>()
                    {
                    });
                    reference.set(list);
                })
                ;
        // @formatter:on

        List<Person> persons = reference.get();
        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#testUserWithPreAuthJSON()
     */
    @Override
    @Test
    void testUserWithPreAuthJSON() throws Exception {
        AtomicReference<List<Person>> reference = new AtomicReference<>(null);

        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .header("my-token", "user")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(result -> {
                    List<Person> list = getObjectMapper().readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>()
                    {
                    });
                    reference.set(list);
                })
                ;
        // @formatter:on

        List<Person> persons = reference.get();
        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#testUserWithPreAuthXML()
     */
    @Override
    @Test
    void testUserWithPreAuthXML() throws Exception {
        ObjectMapper objectMapperXML = getObjectMapperBuilder().createXmlMapper(true).build();
        AtomicReference<List<Person>> reference = new AtomicReference<>(null);

        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .header("my-token", "admin")
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML_VALUE+";charset=UTF-8"))
                .andDo(result -> {
                    List<Person> list = objectMapperXML.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>()
                    {
                    });
                    reference.set(list);
                })
                ;
        // @formatter:on

        List<Person> persons = reference.get();
        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#testUserWithWrongPass()
     */
    @Override
    @Test
    void testUserWithWrongPass() throws Exception {
        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .with(httpBasic("user", "pass")))
                .andExpect(status().isUnauthorized())
                ;
        // @formatter:on
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#testUserWithWrongRole()
     */
    @Override
    @Test
    // @WithMockUser(username = "invalid", password = "pw", roles = "OTHER")
    void testUserWithWrongRole() throws Exception {
        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .with(httpBasic("invalid", "pw")))
            .andExpect(status().isForbidden());
        // @formatter:on
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#testUserWithoutLogin()
     */
    @Override
    @Test
    void testUserWithoutLogin() throws Exception {
        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList"))
            .andExpect(status().isUnauthorized());
        // @formatter:on
    }
}

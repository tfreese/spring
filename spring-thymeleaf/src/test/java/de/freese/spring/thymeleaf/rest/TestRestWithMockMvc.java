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
class TestRestWithMockMvc extends AbstractRestTestCase {
    @Resource
    private MockMvc mockMvc;

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

        final AtomicReference<List<Person>> reference = new AtomicReference<>(null);

        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .with(httpBasic("user", "pw"))
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(result -> {
                    final List<Person> list = getObjectMapper().readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>()
                    {
                    });
                    reference.set(list);
                })
                ;
        // @formatter:on

        final List<Person> persons = reference.get();
        assertNotNull(persons);
        assertTrue(persons.size() >= 3);

        assertEquals("Thomas", persons.getLast().getFirstName());
        assertEquals("Freese", persons.getLast().getLastName());
    }

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

    @Override
    @Test
        // @WithMockUser(username = "user", password = "pw")
    void testUserWithLoginJSON() throws Exception {
        final AtomicReference<List<Person>> reference = new AtomicReference<>(null);

        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .with(httpBasic("user", "pw"))
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(result -> {
                    final List<Person> list = getObjectMapper().readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>()
                    {
                    });
                    reference.set(list);
                })
                ;
        // @formatter:on

        final List<Person> persons = reference.get();
        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    @Override
    @Test
        // @WithMockUser(username = "user", password = "pw")
    void testUserWithLoginXML() throws Exception {
        final ObjectMapper objectMapperXML = getObjectMapperBuilder().createXmlMapper(true).build();
        final AtomicReference<List<Person>> reference = new AtomicReference<>(null);

        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .with(httpBasic("admin", "pw"))
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8"))
                .andDo(result -> {
                    final List<Person> list = objectMapperXML.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>()
                    {
                    });
                    reference.set(list);
                })
                ;
        // @formatter:on

        final List<Person> persons = reference.get();
        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    @Override
    @Test
    void testUserWithPreAuthJSON() throws Exception {
        final AtomicReference<List<Person>> reference = new AtomicReference<>(null);

        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .header("my-token", "user")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(result -> {
                    final List<Person> list = getObjectMapper().readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>()
                    {
                    });
                    reference.set(list);
                })
                ;
        // @formatter:on

        final List<Person> persons = reference.get();
        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    @Override
    @Test
    void testUserWithPreAuthXML() throws Exception {
        final ObjectMapper objectMapperXML = getObjectMapperBuilder().createXmlMapper(true).build();
        final AtomicReference<List<Person>> reference = new AtomicReference<>(null);

        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .header("my-token", "admin")
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML_VALUE+";charset=UTF-8"))
                .andDo(result -> {
                    final List<Person> list = objectMapperXML.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>()
                    {
                    });
                    reference.set(list);
                })
                ;
        // @formatter:on

        final List<Person> persons = reference.get();
        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

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

    @Override
    @Test
        // @WithMockUser(username = "invalid", password = "pw", roles = "OTHER")
    void testUserWithWrongRole() throws Exception {
        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList")
                .with(httpBasic("invalid", "pw")))
                .andExpect(status().isForbidden())
        ;
        // @formatter:on
    }

    @Override
    @Test
    void testUserWithoutLogin() throws Exception {
        // @formatter:off
        this.mockMvc.perform(get("/rest/person/personList"))
                .andExpect(status().isUnauthorized());
        // @formatter:on
    }
}

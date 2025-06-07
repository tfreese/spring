// Created: 14.09.2018
package de.freese.spring.thymeleaf.rest;

import jakarta.annotation.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import de.freese.spring.thymeleaf.ThymeleafApplication;

/**
 * @author Thomas Freese
 */
@SpringBootTest(properties = "server.port=0", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ThymeleafApplication.class)
@AutoConfigureMockMvc
abstract class AbstractRestTestCase {
    @Resource
    private Environment environment;
    @LocalServerPort
    private int localServerPort;

    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private Jackson2ObjectMapperBuilder objectMapperBuilder;

    abstract void testHealthEndpoint() throws Exception;

    abstract void testPost() throws Exception;

    /**
     * User "user" hat keine Berechtigung für "person/personAdd".
     */
    abstract void testPostWithWrongRole() throws Exception;

    /**
     * User "invalid" hat keine Berechtigung für "person/personList".
     */
    abstract void testUserWithLoginJSON() throws Exception;

    abstract void testUserWithLoginXML() throws Exception;

    abstract void testUserWithPreAuthJSON() throws Exception;

    abstract void testUserWithPreAuthXML() throws Exception;

    abstract void testUserWithWrongPass() throws Exception;

    /**
     * User "invalid" hat keine Berechtigung für "person/personList".
     */
    abstract void testUserWithWrongRole() throws Exception;

    abstract void testUserWithoutLogin() throws Exception;

    protected Environment getEnvironment() {
        return environment;
    }

    protected int getLocalServerPort() {
        return localServerPort;
    }

    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    protected Jackson2ObjectMapperBuilder getObjectMapperBuilder() {
        return objectMapperBuilder;
    }
}

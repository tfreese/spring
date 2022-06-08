// Created: 14.09.2018
package de.freese.spring.thymeleaf.rest;

import javax.annotation.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.freese.spring.thymeleaf.ThymeleafApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * @author Thomas Freese
 */
@SpringBootTest(properties = "server.port=0", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ThymeleafApplication.class)
@AutoConfigureMockMvc
abstract class AbstractRestTestCase
{
    /**
     *
     */
    @Resource
    private Environment environment;
    /**
     *
     */
    @LocalServerPort
    private int localServerPort;
    /**
     * Default für JSON.
     */
    @Resource
    private ObjectMapper objectMapper;
    /**
     * Für XML-Mapping
     */
    @Resource
    private Jackson2ObjectMapperBuilder objectMapperBuilder;

    /**
     * @throws Exception Falls was schief geht.
     */
    abstract void testHealthEndpoint() throws Exception;

    /**
     * @throws Exception Falls was schief geht.
     */
    abstract void testPost() throws Exception;

    /**
     * User "user" hat keine Berechtigung für "person/personAdd".
     *
     * @throws Exception Falls was schief geht.
     */
    abstract void testPostWithWrongRole() throws Exception;

    /**
     * User "invalid" hat keine Berechtigung für "person/personList".
     *
     * @throws Exception Falls was schief geht. abstract void test020UserWithWrongRole() throws Exception; /**
     * @throws Exception Falls was schief geht.
     */
    abstract void testUserWithLoginJSON() throws Exception;

    /**
     * @throws Exception Falls was schief geht.
     */
    abstract void testUserWithLoginXML() throws Exception;

    /**
     * @throws Exception Falls was schief geht.
     */
    abstract void testUserWithPreAuthJSON() throws Exception;

    /**
     * @throws Exception Falls was schief geht.
     */
    abstract void testUserWithPreAuthXML() throws Exception;

    /**
     * @throws Exception Falls was schief geht.
     */
    abstract void testUserWithWrongPass() throws Exception;

    /**
     * User "invalid" hat keine Berechtigung für "person/personList".
     *
     * @throws Exception Falls was schief geht.
     */
    abstract void testUserWithWrongRole() throws Exception;

    /**
     * @throws Exception Falls was schief geht.
     */
    abstract void testUserWithoutLogin() throws Exception;

    /**
     * @return Environment
     */
    protected Environment getEnvironment()
    {
        return this.environment;
    }

    /**
     * @return int
     */
    protected int getLocalServerPort()
    {
        return this.localServerPort;
    }

    /**
     * @return com.fasterxml.jackson.databind.ObjectMapper
     */
    protected ObjectMapper getObjectMapper()
    {
        return this.objectMapper;
    }

    /**
     * @return org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
     */
    protected Jackson2ObjectMapperBuilder getObjectMapperBuilder()
    {
        return this.objectMapperBuilder;
    }
}

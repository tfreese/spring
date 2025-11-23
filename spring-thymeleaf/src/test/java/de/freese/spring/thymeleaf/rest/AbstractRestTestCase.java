// Created: 14.09.2018
package de.freese.spring.thymeleaf.rest;

import jakarta.annotation.Resource;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.env.Environment;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.xml.XmlMapper;

import de.freese.spring.thymeleaf.ThymeleafApplication;

/**
 * @author Thomas Freese
 */
@SpringBootTest(properties = "server.port=0", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ThymeleafApplication.class)
@AutoConfigureMockMvc
abstract class AbstractRestTestCase {
    @Resource
    private Environment environment;

    @Resource
    private JsonMapper jsonMapper;

    @LocalServerPort
    private int localServerPort;

    @Resource
    private XmlMapper xmlMapper;

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

    protected JsonMapper getJsonMapper() {
        return jsonMapper;
    }

    protected int getLocalServerPort() {
        return localServerPort;
    }

    protected XmlMapper getXmlMapper() {
        return xmlMapper;
    }
}

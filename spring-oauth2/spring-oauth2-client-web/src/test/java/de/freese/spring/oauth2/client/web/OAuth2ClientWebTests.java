// Created: 31.10.2019
package de.freese.spring.oauth2.client.web;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.MethodName.class)
@ActiveProfiles("test")
@Disabled("Cannot invoke \"java.lang.reflect.Method.invoke(Object, Object[])\" because \"com.sun.xml.bind.v2.runtime.reflect.opt.Injector.defineClass\" is null")
class OAuth2ClientWebTests
{
    /**
     *
     */
    @Test
    void testContextLoads()
    {
        assertTrue(true);
    }
}

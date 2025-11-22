package de.freese.spring.ldap.unboundid;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import de.freese.spring.ldap.unboundid.config.LdapConfig;
import de.freese.spring.ldap.unboundid.controller.HelloController;

/**
 * @author Thomas Freese
 */
@WebMvcTest(HelloController.class)
@Import(LdapConfig.class)
@ActiveProfiles("test")
@Disabled("Cannot invoke \"org.springframework.security.core.Authentication.getName()\" because \"authentication\" is null")
class TestHelloController {
    @Resource
    private MockMvc mvc;

    @Test
    void testRootWhenAuthenticatedThenSaysHelloUser() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("user", "{noop}password")))
                .andExpect(MockMvcResultMatchers.content().string("Hello, user !"));
    }

    @Test
    void testRootWhenUnauthenticatedThen401() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void testTokenWhenBadCredentialsThen401() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("user", "{noop}password_")))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}

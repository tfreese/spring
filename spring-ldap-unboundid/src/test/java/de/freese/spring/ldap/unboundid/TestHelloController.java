package de.freese.spring.ldap.unboundid;

import javax.annotation.Resource;

import de.freese.spring.ldap.unboundid.config.LdapConfig;
import de.freese.spring.ldap.unboundid.controller.HelloController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * @author Thomas Freese
 */
@WebMvcTest(HelloController.class)
@Import(LdapConfig.class)
@ActiveProfiles("test")
class TestHelloController
{
    @Resource
    private MockMvc mvc;

    @Test
    void rootWhenAuthenticatedThenSaysHelloUser() throws Exception
    {
        // @formatter:off
        this.mvc.perform(MockMvcRequestBuilders.get("/")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("user", "password")))
                .andExpect(MockMvcResultMatchers.content().string("Hello, user !"));
        // @formatter:on
    }

    @Test
    void rootWhenUnauthenticatedThen401() throws Exception
    {
        // @formatter:off
        this.mvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        // @formatter:on
    }

    @Test
    void tokenWhenBadCredentialsThen401() throws Exception
    {
        // @formatter:off
        this.mvc.perform(MockMvcRequestBuilders.get("/")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("user", "passwerd")))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        // @formatter:on
    }
}

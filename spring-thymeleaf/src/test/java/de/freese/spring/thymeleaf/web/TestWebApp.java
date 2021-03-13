// Created: 23.01.2018
package de.freese.spring.thymeleaf.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import javax.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.FormLoginRequestBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import de.freese.spring.thymeleaf.ThymeleafApplication;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes =
{
        ThymeleafApplication.class
})
@TestMethodOrder(MethodOrderer.MethodName.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
// @ActiveProfiles(
// {
// "test", "with-ssl"
// })
class TestWebApp
{
    /**
    *
    */
    @LocalServerPort
    private int localServerPort;

    /**
    *
    */
    @Value("${app.message.welcome}")
    private String message = "Hello World";

    /**
    *
    */
    @Resource
    private MockMvc mockMvc;

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testAccessSecuredResourceUnauthenticated() throws Exception
    {
        // @formatter:off
        this.mockMvc.perform(get("/web/person/personList"))
            .andExpect(status().is3xxRedirection())
            .andExpect(unauthenticated())
            .andExpect(redirectedUrlPattern("**/login"));
        // @formatter:on
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testAccessUnsecuredResourceUnauthenticated() throws Exception
    {
        this.mockMvc.perform(get("/")).andExpect(status().isOk());

        // @formatter:off
        this.mockMvc.perform(get("/index"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(this.message)));
        // @formatter:on
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testHealthEndpoint() throws Exception
    {
        // @formatter:off
        this.mockMvc.perform(get("/actuator/health"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").exists())
            .andExpect(jsonPath("$.status").value("UP"));
       // @formatter:on
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testLoginWithBasic() throws Exception
    {
        FormLoginRequestBuilder login = formLogin().loginProcessingUrl("/authenticate").user("admin").password("pw");
        this.mockMvc.perform(login).andExpect(status().is3xxRedirection()).andExpect(authenticated().withUsername("admin"));

        login = formLogin().loginProcessingUrl("/authenticate").user("user").password("pw");
        this.mockMvc.perform(login).andExpect(status().is3xxRedirection()).andExpect(authenticated().withUsername("user"));

        login = formLogin().loginProcessingUrl("/authenticate").user("invalid").password("pw");
        this.mockMvc.perform(login).andExpect(status().is3xxRedirection()).andExpect(authenticated().withUsername("invalid"));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testLoginWithInknownUser() throws Exception
    {
        FormLoginRequestBuilder login = formLogin().loginProcessingUrl("/authenticate").user("unknown").password("pw");

        // @formatter:off
        this.mockMvc.perform(login)
            .andExpect(status().is3xxRedirection())
            .andExpect(unauthenticated())
            .andExpect(redirectedUrlPattern("/login?error=1"));
        // this.mockMvc.perform(login).andExpect(status().isForbidden()).andExpect(unauthenticated());
        // @formatter:on
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testLoginWithPreAuth() throws Exception
    {
        this.mockMvc.perform(get("/web/person/personList").header("my-token", "admin")).andExpect(status().isOk())
                .andExpect(authenticated().withUsername("admin"));

        this.mockMvc.perform(get("/web/person/personList").header("my-token", "user")).andExpect(status().isOk())
                .andExpect(authenticated().withUsername("user"));

        this.mockMvc.perform(get("/web/person/personList").header("my-token", "invalid")).andExpect(status().isOk())
                .andExpect(authenticated().withUsername("invalid"));
    }
}

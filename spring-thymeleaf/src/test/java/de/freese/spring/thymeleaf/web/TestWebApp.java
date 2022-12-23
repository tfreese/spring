// Created: 23.01.2018
package de.freese.spring.thymeleaf.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.annotation.Resource;

import de.freese.spring.thymeleaf.ThymeleafApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.FormLoginRequestBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author Thomas Freese
 */
@SpringBootTest(properties = "server.port=0", webEnvironment = WebEnvironment.MOCK, classes = ThymeleafApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
        // @ActiveProfiles(
        // {
        // "test", "with-ssl"
        // })
class TestWebApp
{
    //    @LocalServerPort
    //    private int localServerPort;

    @Resource
    private MockMvc mockMvc;

    @Test
    void testAccessSecuredResourceUnauthenticated() throws Exception
    {
        // @formatter:off
        this.mockMvc.perform(get("/web/person/personList"))
                .andDo(print())
                //.andExpect(status().is3xxRedirection())
                .andExpect(status().isUnauthorized())
                .andExpect(unauthenticated())
                //.andExpect(redirectedUrlPattern("**/login"))
                ;
        // @formatter:on
    }

    @Test
    void testAccessUnsecuredResourceUnauthenticated() throws Exception
    {
        this.mockMvc.perform(get("/")).andExpect(status().isOk());

        // @formatter:off
        this.mockMvc.perform(get("/index"))
            .andDo(print())
            .andExpect(status().isOk())
            //.andExpect(content().string(containsString(this.message)))
            ;
        // @formatter:on
    }

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

    @Test
    void testLoginWithUnknownUser() throws Exception
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
}

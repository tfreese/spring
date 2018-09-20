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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.FormLoginRequestBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import de.freese.spring.thymeleaf.ThymeleafApplication;

/**
 * @author Thomas Freese
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes =
{
        ThymeleafApplication.class
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
// @ActiveProfiles(
// {
// "test", "with-ssl"
// })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestWebApp
{
    /**
    *
    */
    @LocalServerPort
    private int localServerPort = 0;

    /**
    *
    */
    @Value("${app.message.welcome}")
    private String message = "Hello World";

    /**
    *
    */
    @Resource
    private MockMvc mockMvc = null;

    /**
     * Erzeugt eine neue Instanz von {@link TestWebApp}.
     */
    public TestWebApp()
    {
        super();
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void accessSecuredResourceUnauthenticated() throws Exception
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
    public void accessUnsecuredResourceUnauthenticated() throws Exception
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
    public void healthEndpoint() throws Exception
    {
        // @formatter:off
        this.mockMvc.perform(get("/actuator/health"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").exists())
            .andExpect(jsonPath("$.status").value("UP"));
       // @formatter:on
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void loginWithBasic() throws Exception
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
    public void loginWithInknownUser() throws Exception
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
    public void loginWithPreAuth() throws Exception
    {
        this.mockMvc.perform(get("/web/person/personList").header("my-token", "admin")).andExpect(status().isOk())
                .andExpect(authenticated().withUsername("admin"));

        this.mockMvc.perform(get("/web/person/personList").header("my-token", "user")).andExpect(status().isOk())
                .andExpect(authenticated().withUsername("user"));

        this.mockMvc.perform(get("/web/person/personList").header("my-token", "invalid")).andExpect(status().isOk())
                .andExpect(authenticated().withUsername("invalid"));
    }
}

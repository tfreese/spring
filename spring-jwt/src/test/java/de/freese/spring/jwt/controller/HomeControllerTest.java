package de.freese.spring.jwt.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import de.freese.spring.jwt.config.SecurityConfig;
import de.freese.spring.jwt.service.TokenService;

@WebMvcTest({HomeController.class, TokenController.class})
@Import({SecurityConfig.class, TokenService.class})
class HomeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void testAdmin() throws Exception {
        MvcResult result = mvc.perform(post("/token").with(httpBasic("admin", "adminpw"))).andExpect(status().isOk()).andReturn();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

        final String token = result.getResponse().getContentAsString();

        result = mvc.perform(get("/admin").with(request -> {
            request.addHeader("Authorization", "Bearer " + token);
            return request;
        })).andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

        result = mvc.perform(get("/user").with(request -> {
            request.addHeader("Authorization", "Bearer " + token);
            return request;
        })).andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).isNotEmpty();
    }

    @Test
    void testAdminWithBasicStatusIsUnauthorized() throws Exception {
        mvc.perform(get("/").with(httpBasic("admin", "adminpw"))).andExpect(status().isUnauthorized());
    }

    @Test
    void testTokenWhenAnonymousThenStatusIsUnauthorized() throws Exception {
        mvc.perform(post("/token")).andExpect(status().isUnauthorized());
    }

    @Test
    void testTokenWithBasicThenGetToken() throws Exception {
        final MvcResult result = mvc.perform(post("/token").with(httpBasic("admin", "adminpw"))).andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).isNotEmpty();
    }

    @Test
    void testUser() throws Exception {
        MvcResult result = mvc.perform(post("/token").with(httpBasic("user", "userpw"))).andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

        final String token = result.getResponse().getContentAsString();

        mvc.perform(get("/admin").with(request -> {
            request.addHeader("Authorization", "Bearer " + token);
            return request;
        })).andExpect(status().isForbidden());

        result = mvc.perform(get("/user").with(request -> {
            request.addHeader("Authorization", "Bearer " + token);
            return request;
        })).andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).isNotEmpty();
    }

    @Test
    void testWhenUnauthenticatedThenUnauthorized() throws Exception {
        mvc.perform(get("/")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testWithMockUserStatusIsOK() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
    }
}

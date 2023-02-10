// Created: 14.02.2017
package de.freese.spring.cloud.microservice;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = MicroServiceApplication.class, properties = {})
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
class TestRestController {
    @Resource
    private MockMvc mockMvc;

    // @Resource
    // private WebTestClient webClient;

    @Test
    void testHello() throws Exception {
        // @formatter:off
        this.mockMvc.perform(
                            get("/") // Test-URLs ohne Context-Root angeben
                            .accept(MediaType.APPLICATION_JSON)
                            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString("Hello from")))
            .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
        ;
        // @formatter:on
    }

    @Test
    void testPing() throws Exception {
        // .andDo(print()).andExpect(jsonPath("$.content").value("Hello, Spring Community!"));

        // @formatter:off
        this.mockMvc.perform(get("/ping")) // Test-URLs ohne Context-Root angeben.
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//          .andDo(MockMvcResultHandlers.print())
            .andExpect(content().string(containsString("Ping from")))
            ;
//        this.webClient.get().repository("/")
//            .exchange()
//            .expectStatus().isOk()
//            .exp
//            .expectBody(String.class).isEqualTo("true");
        // @formatter:on
    }
}

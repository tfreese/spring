// Created: 14.02.2017
package de.freese.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.annotation.Resource;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = MicroServiceApplication.class, properties = {})
@TestMethodOrder(MethodOrderer.MethodName.class)
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
class TestRestService {
    @Resource
    private MockMvc mockMvc;

    @LocalServerPort
    private int port;

    @Resource
    private RestTemplateBuilder restTemplateBuilder;

    @Resource
    private WebClient.Builder webClientBuilder;

    @Test
    void testBenchmark() throws Exception {
        // @formatter:off
        Options options = new OptionsBuilder()
                .include(MicroServiceBenchmark.class.getSimpleName())
                //.include("\\." + this.getClass().getSimpleName() + "\\.") für Benchmark in dieser Junit-Klasse
                //.addProfiler(GCProfiler.class)
                //.addProfiler(HotspotMemoryProfiler.class)
                .shouldFailOnError(true)
                .warmupIterations(0)
                .warmupTime(TimeValue.milliseconds(100))
                .measurementIterations(1)
                .measurementTime(TimeValue.milliseconds(200))
                .jvmArgs("-Dserver.port=11111")
//                .threads(1)
                .forks(0)
//                .resultFormat(ResultFormatType.CSV)
//                .result("/dev/null")
                .build()
                ;
        // @formatter:on

        new Runner(options).run();
    }

    @Test
    void testMockMvc() throws Exception {
        // .andDo(print()).andExpect(jsonPath("$.content").value("Hello, Spring Community!"));

        // @formatter:off
        this.mockMvc.perform(get("/")) // Test-URLs ohne Context-Root angeben.
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//          .andDo(MockMvcResultHandlers.print())
            .andExpect(content().string("Hello, World"))
            .andExpect(MockMvcResultMatchers.jsonPath("$").value("Hello, World")) // Alternative zu string("true")
            .andExpect(MockMvcResultMatchers.jsonPath("$").value(Matchers.is("Hello, World"))); // Alternative zu string("true")
        // @formatter:on
    }

    @Test
    void testRestTemplate() throws Exception {
        // TestRestTemplate restTemplate = new TestRestTemplate(this.restTemplateBuilder.rootUri("http://localhost:" + this.port));
        RestTemplate restTemplate = this.restTemplateBuilder.rootUri("http://localhost:" + this.port).build();

        // String result = restTemplate.getForObject("/",String.class);
        ResponseEntity<String> response = restTemplate.getForEntity("/", String.class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Hello, World", response.getBody());
    }

    @Test
    void testWebClient() throws Exception {
        WebClient webClient = this.webClientBuilder.baseUrl("http://localhost:" + this.port).build();

        // String response = webClient.get().uri("/").retrieve().bodyToMono(String.class).block();
        ResponseEntity<String> response = webClient.get().uri("/").exchangeToMono(clientResponse -> clientResponse.toEntity(String.class)).block();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Hello, World", response.getBody());
    }
}

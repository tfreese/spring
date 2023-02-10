// Created: 29.11.2021
package de.freese.spring.hateoas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.annotation.Resource;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

/**
 * @author Thomas Freese
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GreetingMockMvcTests {
    @Resource
    private MockMvc mockMvc;

    @Test
    void testDefault() throws Exception {
        // @formatter:off
        this.mockMvc.perform(get("/greeter"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("greeting").value("Hello, World!"))
            .andExpect(jsonPath("_links.self.href").value("http://localhost/greeter?name=World"))
            .andExpect(jsonPath("_links.forPath.href").value("http://localhost/greeter/path/World"))
            .andExpect(jsonPath("_links.forPojo.href").value("http://localhost/greeter/pojo?name=World"))
            .andExpect(jsonPath("_links.forSimple.href").value("http://localhost/greeter/simple?name=World"))
            ;
        // @formatter:end
    }

    @Test
    void testFail() throws Exception
    {
        // @formatter:off
        this.mockMvc.perform(get("/greeter/fail"))
            //.andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest())
            ;
        // @formatter:end
    }

    @Test
    void testJsonPath() throws Exception
    {
        // @formatter:off
        String response = this.mockMvc.perform(get("/greeter/simple"))
            //.andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString()
            ;
        // @formatter:on

        // System.out.println(response);

        // CacheProvider.setCache für intern erzeugte JsonPath Instanzen
        DocumentContext documentContext = JsonPath.parse(response);
        assertEquals("Hello, World!", documentContext.read("greeting", String.class));

        // Hier greift der Cache nicht.
        // JsonPath jsonPathGreeting = JsonPath.compile("greeting");
        // assertEquals("Hello, World!", documentContext.read(jsonPathGreeting, String.class));
        // assertEquals("Hello, World!", JsonPath.compile("greeting").read(response));
    }

    @Test
    void testPath() throws Exception {
        // @formatter:off
       this.mockMvc.perform(get("/greeter/path/Test"))
           //.andDo(MockMvcResultHandlers.print())
           .andExpect(status().isOk())
           .andExpect(jsonPath("greeting").value("Hello, Test!"))
           .andExpect(jsonPath("_links.self.href").value("http://localhost/greeter/path/Test"))
           .andExpect(jsonPath("_links.forPojo.href").value("http://localhost/greeter/pojo?name=Test"))
           .andExpect(jsonPath("_links.forSimple.href").value("http://localhost/greeter/simple?name=Test"))
           ;
       // @formatter:on
    }

    @Test
    void testPojo() throws Exception {
        // @formatter:off
        this.mockMvc.perform(get("/greeter/pojo").param("name", "Test"))
            //.andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("greeting").value("Hello, Test!"))
            .andExpect(jsonPath("_links.self.href").value("http://localhost/greeter/pojo?name=Test"))
            .andExpect(jsonPath("_links.forPath.href").value("http://localhost/greeter/path/Test"))
            .andExpect(jsonPath("_links.forSimple.href").value("http://localhost/greeter/simple?name=Test"))
            ;
        // @formatter:on
    }

    @Test
    void testSimple() throws Exception {
        // @formatter:off
        this.mockMvc.perform(get("/greeter/simple"))
            //.andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("greeting").value("Hello, World!"))
            ;

        this.mockMvc.perform(get("/greeter/simple").param("name", "Test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("greeting").value("Hello, Test!"))
            ;
        // @formatter:on
    }
}

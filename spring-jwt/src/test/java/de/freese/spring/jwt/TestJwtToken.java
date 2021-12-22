// Created: 07.09.2018
package de.freese.spring.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.jayway.jsonpath.internal.JsonFormatter;

import de.freese.spring.jwt.token.JwtTokenProvider;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = JwtAuthorisationApplication.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
interface TestJwtToken
{
    /**
     * @return {@link JwtTokenProvider}
     */
    JwtTokenProvider getJwtTokenProvider();

    /**
     * @return {@link MockMvc}
     */
    MockMvc getMockMvc();

    /**
     * @return {@link RestTemplateBuilder}
     */
    RestTemplateBuilder getRestTemplateBuilder();

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    default void testFailNoLogin() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = getRestTemplateBuilder()
//                .interceptors(
//                        //new HttpHeaderInterceptor("Authorization", "Bearer " + this.tokenProvider.createToken("user", "pass")),
//                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build()
                ;
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/jwt/users/me", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());

        if (responseEntity.hasBody())
        {
            System.out.printf("%nFail: %s%n", responseEntity.getBody());
        }

        // @formatter:off
        String response = getMockMvc().perform(get("/jwt/users/me")
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andReturn()
            .getResponse()
            .getContentAsString()
            ;
        // @formatter:end

        System.out.printf("%nFail: %s%n", response);
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    default void testFailWrongPass() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = getRestTemplateBuilder()
                .defaultHeader("Authorization", "Bearer " + getJwtTokenProvider().createToken("admin", "pas"))
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build()
                ;
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/jwt/users/me", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        // UTF-8 kommt aus WebSecurityComponentsConfig.RestAuthenticationEntryPoint.commence.
        assertEquals(MediaType.APPLICATION_JSON + ";charset=UTF-8", responseEntity.getHeaders().getFirst("Content-Type"));

        System.out.printf("%nWrong Pass: %s%n", responseEntity.getBody());

//        // @formatter:off
//        getMockMvc().perform(get("/jwt/users/me"))
//            .andDo(print())
//            .andExpect(status().isUnauthorized())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//            .andExpect(content().string("HTTP Status 401 - An Authentication object was not found in the SecurityContext"))
//            ;
//        // @formatter:end
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    default void testLoginAdmin() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = getRestTemplateBuilder()
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build()
                ;
        // @formatter:on

        String uri = UriComponentsBuilder.fromPath("/jwt/users/login").queryParam("username", "admin").queryParam("password", "pass").toUriString();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8", responseEntity.getHeaders().getFirst("Content-Type"));

        System.out.printf("%nLogin Admin: %s%n", responseEntity.getBody());
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    default void testLoginUser() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = getRestTemplateBuilder()
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build()
                ;
        // @formatter:on

        String uri = UriComponentsBuilder.fromPath("/jwt/users/login").queryParam("username", "user").queryParam("password", "pass").toUriString();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8", responseEntity.getHeaders().getFirst("Content-Type"));

        System.out.printf("%nLogin User: %s%n", responseEntity.getBody());
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    default void testMeAdmin() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = getRestTemplateBuilder()
                .defaultHeader("Authorization", "Bearer " + getJwtTokenProvider().createToken("admin", "pass"))
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build()
                ;
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/jwt/users/me", String.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getFirst("Content-Type"));

        System.out.printf("%nMe Admin: %s%n", JsonFormatter.prettyPrint(responseEntity.getBody()));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    default void testMeUser() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = getRestTemplateBuilder()
                .defaultHeader("Authorization", "Bearer " + getJwtTokenProvider().createToken("user", "pass"))
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build()
                ;
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/jwt/users/me", String.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getFirst("Content-Type"));

        System.out.printf("%nMe User: %s%n", JsonFormatter.prettyPrint(responseEntity.getBody()));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    default void testSearchAdmin() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = getRestTemplateBuilder()
                .defaultHeader("Authorization", "Bearer " + getJwtTokenProvider().createToken("admin", "pass"))
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build()
                ;
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/jwt/users/search/user", String.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getFirst("Content-Type"));

        System.out.printf("%nSearch Admin: %s%n", JsonFormatter.prettyPrint(responseEntity.getBody()));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    default void testSearchUser() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = getRestTemplateBuilder()
                .defaultHeader("Authorization", "Bearer " + getJwtTokenProvider().createToken("user", "pass"))
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build()
                ;
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/jwt/users/search/user", String.class);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());

        System.out.printf("%nSearch User: %s%n", responseEntity.getBody());
    }
}

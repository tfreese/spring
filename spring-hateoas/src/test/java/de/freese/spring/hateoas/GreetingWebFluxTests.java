// Created: 29.11.2021
package de.freese.spring.hateoas;

import javax.annotation.Resource;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * @author Thomas Freese
 */
//@SpringBootTest
//@WebFluxTest(GreetingController.class) // braucht spring-boot-starter-webflux
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
class GreetingWebFluxTests
{
    /**
     *
     */
    @Resource
    private WebTestClient webTestClient;

    /**
     * @throws Exception Falls was schief geht.
     */
    //@Test
    void testSimple() throws Exception
    {
        // @formatter:off
        String response = this.webTestClient.get()
            .uri("/greeter/simple")
            .accept(MediaType.APPLICATION_JSON)
            //.body(BodyInserters.fromObject("is"))
            .exchange()
            .expectStatus().isOk()
            .returnResult(String.class)
            .getResponseBody()
            .blockFirst()
            ;
        // @formatter:on

        System.out.println(response);
    }
}

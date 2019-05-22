package de.freese.kubernetes.microservice;

import javax.annotation.Resource;
import org.hamcrest.core.StringStartsWith;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import junit.framework.TestCase;

/**
 * @author Thomas Freese
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MyApplicationTest extends TestCase
{
    /**
     *
     */
    @Resource
    private WebTestClient webTestClient;

    /**
     * Erstellt ein neues {@link MyApplicationTest} Object.
     */
    public MyApplicationTest()
    {
        super();
    }

    /**
     *
     */
    @Test
    public void contextLoads()
    {
    }

    /**
    *
    */
    @Test
    public void request()
    {
        //@formatter:off
        this.webTestClient
            .get()
            .uri("/greet")
            .accept(MediaType.TEXT_PLAIN)
            .exchange()
            .expectStatus().isOk()
            //.expectBody(String.class).isEqualTo("Hello, Spring!")
            .expectBody(String.class).value(StringStartsWith.startsWith("Hello World"))
            ;
        //@formatter:on
    }
}

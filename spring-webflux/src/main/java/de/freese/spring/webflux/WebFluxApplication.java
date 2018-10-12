// Created: 14.02.2017
package de.freese.spring.webflux;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import reactor.core.publisher.Mono;

/**
 * Demo mit eigenem LoadBalancer.
 *
 * @author Thomas Freese
 */
public class WebFluxApplication
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebFluxApplication.class);

    /**
     * @param args String[]
     * @throws Exception Falls was schief geht.
     */
    public static void main(final String[] args) throws Exception
    {
        Consumer<String> consumer = value -> {

            LOGGER.info(value);

            if (value == null)
            {
                // break;
                System.exit(0);
            }

        };

        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(WebFluxApplication.class).run(args))
        {
            WebClient webClient = context.getBean("webClient", WebClient.class);
            RequestHeadersSpec<?> request = webClient.get().uri("/netflix/service/sysdate"); // .accept(MediaType.APPLICATION_JSON)

            while (true)
            {
                // @formatter:off
                Mono<String> result = request
                        .exchange()
                        .flatMap(response -> response.bodyToMono(String.class));
                // @formatter:on

                result.subscribe(consumer);
                // consumer.accept(result.block());

                Thread.sleep(3000L);
            }
        }
    }

    /**
     * Erzeugt eine neue Instanz von {@link WebFluxApplication}
     */
    public WebFluxApplication()
    {
        super();
    }

    /**
     * @return {@link WebClient}
     */
    @Bean
    public WebClient webClient()
    {
        // https://github.com/spring-cloud/spring-cloud-commons/blob/master/spring-cloud-commons/src/main/java/org/springframework/cloud/client/loadbalancer/reactive/LoadBalancerExchangeFilterFunction.java
        // LoadBalancerExchangeFilterFunction lbFunction;

        // WebClient client = WebClient.create("http://localhost:65501");

        // @formatter:off
        WebClient client = WebClient.builder()
                .baseUrl("http://localhost:65501")
                .defaultHeader(HttpHeaders.USER_AGENT, "Spring 5 WebClient")
//                .filter(lbFunction)
                .build();
        // @formatter:on

        return client;
    }
}

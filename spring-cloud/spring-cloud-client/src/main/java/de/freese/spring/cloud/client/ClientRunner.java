// Created: 10.10.2021
package de.freese.spring.cloud.client;

import java.time.Duration;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@Component
//@Order(1)
@Profile("!test")
public class ClientRunner implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRunner.class);

    @Resource
    private LoadBalancedExchangeFilterFunction loadBalancedFunction;
    @Resource
    private ReactiveLoadBalancer.Factory<ServiceInstance> serviceInstanceFactory;
    @Resource
    private WebClient.Builder webClientBuilder;
    @Resource
    //    @LoadBalanced
    private WebClient.Builder webClientBuilderLoadBalanced;

    @Override
    public void run(final ApplicationArguments args) {
        // Die Erzeugung im Konstruktor funktioniert nicht, da dort die WebClient.Builder noch nicht fertig konfiguriert sind.
        final WebClient webClient = webClientBuilder.build();
        final WebClient webClientLoadBalanced = webClientBuilderLoadBalanced.clone().baseUrl("http://CLOUD-HELLO-SERVICE").build();
        final WebClient webClientWithLoadBalancedFunction = webClientBuilder.clone().baseUrl("http://CLOUD-HELLO-SERVICE").filter(this.loadBalancedFunction).build();

        for (int i = 0; i < 4; i++) {
            runServiceDiscovery(webClient);
        }

        for (int i = 0; i < 4; i++) {
            runWebClientWithLoadBalancer(webClientLoadBalanced);
        }

        for (int i = 0; i < 4; i++) {
            runWebClientWithLoadBalancedFunction(webClientWithLoadBalancedFunction);
        }
    }

    private String call(final WebClient webClient, final String uri) {
        // return webClient
        //         .get()
        //         .uri(uri)
        //         .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
        //         .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(100)))
        //         .timeout(Duration.ofMillis(1000), Mono.just("fallback"))
        //         .onErrorResume(throwable -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("fallback")))
        //         .block()
        //         .getBody()
        //         ;

        return webClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                //.retryWhen(Retry.fixedDelay(2, Duration.ofMillis(200)))
                .timeout(Duration.ofMillis(1000L), Mono.just("fallback"))
                .onErrorResume(throwable -> {
                    LOGGER.error(throwable.getMessage());
                    return Mono.just("fallback");
                })
                .block()
                //.subscribe(response -> LOGGER.info("call: {}", response.strip()) )
                ;
    }

    private void runServiceDiscovery(final WebClient webClient) {
        final ReactiveLoadBalancer<ServiceInstance> loadBalancer = this.serviceInstanceFactory.getInstance("CLOUD-HELLO-SERVICE");

        final String url = Mono.from(loadBalancer.choose())
                .map(Response::getServer)
                .map(server ->
                {
                    final String protocol = server.isSecure() ? "https" : "http";

                    return String.format("%s://%s:%d/", protocol, server.getHost(), server.getPort());
                })
                .onErrorResume(throwable -> Mono.empty())
                .block();

        final String response = call(webClient, url);

        LOGGER.info("runServiceDiscovery: {}", response.strip());
    }

    private void runWebClientWithLoadBalancedFunction(final WebClient webClient) {
        final String response = call(webClient, "/");

        LOGGER.info("runWebClientWithLoadBalancedFunction: {}", response.strip());
    }

    private void runWebClientWithLoadBalancer(final WebClient webClient) {
        final String response = call(webClient, "/");

        LOGGER.info("runWebClientWithLoadBalancer: {}", response.strip());
    }
}

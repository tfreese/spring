// Created: 10.10.2021
package de.freese.spring.gateway;

import java.time.Duration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * @author Thomas Freese
 */
@Component
@Order(1)
@Profile("!test")
public class GatewayApplicationRunner implements ApplicationRunner
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayApplicationRunner.class);
    /**
     *
     */
    @Resource
    private ReactorLoadBalancerExchangeFilterFunction loadBalancedFunction;
    /**
     *
     */
    @Resource
    private WebClient.Builder webClientBuilder;
    /**
     *
     */
    @Resource
    private WebClient.Builder webClientBuilderLoadBalanced;
    /**
     *
     */
    private WebClient webClient;
    /**
     *
     */
    private WebClient webClientWithLoadBalancedFunction;
    /**
     *
     */
    private WebClient webClientLoadBalanced;
    /**
     *
     */
    @Resource
    private ReactiveLoadBalancer.Factory<ServiceInstance> serviceInstanceFactory;

    /**
     *
     */
    @PostConstruct
    void postConstruct()
    {
        // Die Erzeugung im Konstruktor funktioniert nicht, da dort die WebClient.Builder noch nicht fertig konfiguriert sind.
        this.webClient = webClientBuilder.build();
        this.webClientWithLoadBalancedFunction = webClientBuilder.clone().filter(this.loadBalancedFunction).baseUrl("http://HELLO-SERVICE").build();
        this.webClientLoadBalanced = webClientBuilderLoadBalanced.clone().baseUrl("http://HELLO-SERVICE").build();
    }

    private String call(WebClient webClient, String uri)
    {
        // @formatter:off
//        return webClient
//                .get()
//                .uri(uri)
//                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
//                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(100)))
//                .timeout(Duration.ofMillis(1000), Mono.just("fallback"))
//                .onErrorResume(throwable -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("fallback")))
//                .block()
//                .getBody()
//                ;
         //@formatter:on

        // @formatter:off
        return webClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(200)))
                .timeout(Duration.ofMillis(1000), Mono.just("fallback"))
                .onErrorResume(throwable -> {
                    LOGGER.error(throwable.getMessage());
                    return Mono.just("fallback");
                })
                .block()
                //.subscribe(response -> LOGGER.info("call: {}", response.strip()) )
                ;
        // @formatter:on
    }

    /**
     *
     */
    private void callByWebClientWithLoadBalancer()
    {
        String response = call(this.webClientLoadBalanced, "/");

        LOGGER.info("callByWebClientWithLoadBalancer: {}", response.strip());
    }

    /**
     *
     */
    private void callByWebClientWithLoadBalancedFunction()
    {
        String response = call(this.webClientWithLoadBalancedFunction, "/");

        LOGGER.info("callByWebClientWithLoadBalancedFunction: {}", response.strip());
    }

    /**
     *
     */
    private void callByServiceDiscovery()
    {
        ReactiveLoadBalancer<ServiceInstance> loadBalancer = this.serviceInstanceFactory.getInstance("HELLO-SERVICE");

        // @formatter:off
        String url = Mono.from(loadBalancer.choose())
                .map(Response::getServer)
                .map(server ->
                {
                    String protocol = server.isSecure() ? "https" : "http";

                    return protocol + "://" + server.getHost() + ':' + server.getPort() + "/";
                })
                .onErrorResume(throwable -> Mono.empty())
                .block()
                ;
        // @formatter:on

        String response = call(this.webClient, url);

        LOGGER.info("callByServiceDiscovery: {}", response.strip());
    }

    /**
     * @see org.springframework.boot.ApplicationRunner#run(org.springframework.boot.ApplicationArguments)
     */
    @Override
    public void run(final ApplicationArguments args) throws Exception
    {
        for (int i = 0; i < 4; i++)
        {
            callByWebClientWithLoadBalancer();
        }

        for (int i = 0; i < 4; i++)
        {
            callByWebClientWithLoadBalancedFunction();
        }

        for (int i = 0; i < 4; i++)
        {
            callByServiceDiscovery();
        }
    }
}

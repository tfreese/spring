// Created: 10.10.2021
package de.freese.spring.gateway;

import java.time.Duration;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
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
    private WebClient.Builder loadBalancedWebClientBuilder;
    /**
     *
     */
    @Resource
    private ReactiveLoadBalancer.Factory<ServiceInstance> serviceInstanceFactory;

    /**
     *
     */
    private void callByWebClientWithLoadBalancer()
    {
        // @formatter:off
//        ResponseEntity<String> response = this.loadBalancedWebClientBuilder.build()
//                .get()
//                .uri("http://HELLO-SERVICE")
//                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
//                .block()
//                ;
        // @formatter:on

        // @formatter:off
        String response = this.loadBalancedWebClientBuilder.build()
                .get()
                .uri("http://HELLO-SERVICE")
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(100)))
                //.timeout(Duration.ofMillis(200), Mono.just("fallback"))
                .onErrorResume(throwable -> Mono.just("fallback"))
                .block()
                //.subscribe(response -> LOGGER.info("callByWebClientWithLoadBalancer: {}", response.strip()) )
                ;
        // @formatter:on

        LOGGER.info("callByWebClientWithLoadBalancer: {}", response.strip());
    }

    /**
     *
     */
    private void callByWebClientWithLoadBalancedFunction()
    {
        // @formatter:off
        String response = WebClient.builder()
                .filter(this.loadBalancedFunction)
                .baseUrl("http://HELLO-SERVICE")
                .build()
                .get()
                .uri("/")
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(100)))
                //.timeout(Duration.ofMillis(200), Mono.just("fallback"))
                .onErrorResume(throwable -> Mono.just("fallback"))
                .block()
                //.subscribe(response -> LOGGER.info("callByWebClientWithLoadBalancedFunction: {}", response.strip()) )
                ;
        // @formatter:on

        LOGGER.info("callByWebClientWithLoadBalancedFunction: {}", response.strip());
    }

    /**
     *
     */
    private void callByServiceDiscovery()
    {
        ReactiveLoadBalancer<ServiceInstance> loadBalancer = this.serviceInstanceFactory.getInstance("HELLO-SERVICE");
        Mono<Response<ServiceInstance>> chosen = Mono.from(loadBalancer.choose());

        // @formatter:off
        String url = chosen.map(serviceInstance ->
                {
                    ServiceInstance server = serviceInstance.getServer();

                    if (server == null)
                    {
                        return "";
                    }

                    return "http://" + server.getHost() + ':' + server.getPort() + "/";
                })
                .block()
                ;
        // @formatter:on

        // @formatter:off
        String response = WebClient.builder()
                .baseUrl(url)
                .build()
                .get()
                .uri("/")
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(100)))
                //.timeout(Duration.ofMillis(200), Mono.just("fallback"))
                .onErrorResume(throwable -> Mono.just("fallback"))
                .block()
                ;
        // @formatter:on

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

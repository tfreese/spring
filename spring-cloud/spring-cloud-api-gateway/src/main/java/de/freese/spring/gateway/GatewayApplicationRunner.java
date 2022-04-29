// Created: 10.10.2021
package de.freese.spring.gateway;

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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
    private ReactorLoadBalancerExchangeFilterFunction lbFunction;

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
     * @param http {@link WebClient}
     * @param url String
     *
     * @return {@link Mono}
     */
    private Mono<String> call(final WebClient http, final String url)
    {
        if (url.isBlank())
        {
            return Mono.just("url is blank");
        }

        return http.get().uri(url).retrieve().bodyToMono(String.class);
    }

    /**
     *
     */
    private void checkLoadBalancer()
    {
        // LoadBalancing über WebFilter
        ResponseEntity<String> response = WebClient.builder().filter(this.lbFunction).baseUrl("http://DATE-SERVICE").build().get().uri("/service/sysdate")
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class)).block();
        LOGGER.info(response.getBody());

        // Fertige Builder-Instanz, Funktioniert nur zusammen mit MyWebClientConfig, aber dann klappt gar nix mehr !
        //
        // response = this.loadBalancedWebClientBuilder.build().get().uri("http://DATE-SERVICE/service/sysdate")
        // .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class)).block();
        // LOGGER.info(response.getBody());
    }

    /**
     *
     */
    private void lookupServiceDiscovery()
    {
        WebClient http = WebClient.builder().build();

        ReactiveLoadBalancer<ServiceInstance> loadBalancer = this.serviceInstanceFactory.getInstance("DATE-SERVICE");
        Mono<Response<ServiceInstance>> chosen = Mono.from(loadBalancer.choose());

        chosen.map(serviceInstance ->
        {
            ServiceInstance server = serviceInstance.getServer();

            if (server == null)
            {
                return "";
            }

            String url = "http://" + server.getHost() + ':' + server.getPort() + "/service/sysdate";
            LOGGER.info(url);

            return url;
        }).flatMap(url -> call(http, url)).subscribe(result -> LOGGER.info("{}", result));
    }

    /**
     * @see org.springframework.boot.ApplicationRunner#run(org.springframework.boot.ApplicationArguments)
     */
    @Override
    public void run(final ApplicationArguments args) throws Exception
    {
        lookupServiceDiscovery();
        checkLoadBalancer();
    }
}

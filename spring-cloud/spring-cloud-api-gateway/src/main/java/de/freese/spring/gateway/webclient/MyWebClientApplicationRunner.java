// Created: 10.10.2021
package de.freese.spring.gateway.webclient;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Thomas Freese
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class MyWebClientApplicationRunner implements ApplicationRunner
{
    /**
    *
    */
    private static final Logger LOGGER = LoggerFactory.getLogger(MyWebClientApplicationRunner.class);
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
     * @see org.springframework.boot.ApplicationRunner#run(org.springframework.boot.ApplicationArguments)
     */
    @Override
    public void run(final ApplicationArguments args) throws Exception
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
}

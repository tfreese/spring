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
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ReactiveLoadBalancerFactoryRunner implements ApplicationRunner
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveLoadBalancerFactoryRunner.class);
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
        return http.get().uri(url).retrieve().bodyToMono(String.class);
    }

    /**
     * @see org.springframework.boot.ApplicationRunner#run(org.springframework.boot.ApplicationArguments)
     */
    @Override
    public void run(final ApplicationArguments args) throws Exception
    {
        WebClient http = WebClient.builder().build();

        ReactiveLoadBalancer<ServiceInstance> loadBalancer = this.serviceInstanceFactory.getInstance("DATE-SERVICE");
        Mono<Response<ServiceInstance>> chosen = Mono.from(loadBalancer.choose());

        chosen.map(serviceInstance -> {
            ServiceInstance server = serviceInstance.getServer();
            String url = "http://" + server.getHost() + ':' + server.getPort() + "/service/sysdate";
            LOGGER.info(url);

            return url;
        }).flatMap(url -> call(http, url)).subscribe(result -> LOGGER.info("{}", result));
    }
}

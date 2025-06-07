// Created: 10.10.2021
package de.freese.spring.cloud.client.config;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * @author Thomas Freese
 */
@Configuration
@LoadBalancerClients(@LoadBalancerClient(name = "CLOUD-HELLO-SERVICE"/*, configuration = HelloServiceInstanceListSupplierConfig.class)*/))
public class WebClientConfig {
    /**
     * Siehe unten "Bugfix: ReactiveRegistrationClient".<br>
     * Ein Default-WebClient wird immer benötigt.
     */
    @Bean
    @Primary
    WebClient.Builder webClientBuilder() {
        final HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
                .doOnConnected(connection ->
                        connection
                                .addHandlerLast(new ReadTimeoutHandler(3L, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(3L, TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                ;
    }

    @Bean
    @LoadBalanced
    @SuppressWarnings("java:S4144")
    WebClient.Builder webClientBuilderLoadBalanced() {
        final HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
                .doOnConnected(connection ->
                        connection
                                .addHandlerLast(new ReadTimeoutHandler(3L, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(3L, TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                ;
    }

    //    /**
    //     * @see LoadBalancerClientConfiguration#reactorServiceInstanceLoadBalancer(Environment, LoadBalancerClientFactory)
    //     * @see MyServiceInstanceListSupplier
    //     */
    //    @Bean
    //    ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(Environment environment
    //      , LoadBalancerClientFactory loadBalancerClientFactory
    //      , ServiceInstanceListSupplier serviceInstanceListSupplier) {
    //        //        final String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
    //        final String name = serviceInstanceListSupplier.getServiceId();
    //
    //        return new RandomLoadBalancer(loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
    //    }

    //    /**
    //     * Bugfix: ReactiveRegistrationClient von boot.admin funktioniert nicht mit LoadBalanced WebClient.
    //     *
    //     * @see de.codecentric.boot.admin.client.config.SpringBootAdminClientAutoConfiguration.ReactiveRegistrationClientConfig
    //     */
    //    @Bean
    //    public RegistrationClient registrationClient(ClientProperties client) {
    //        return new ReactiveRegistrationClient(WebClient.builder().build(), client.getReadTimeout());
    //    }
}

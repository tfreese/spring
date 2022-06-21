// Created: 10.10.2021
package de.freese.spring.gateway.webclient;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClientConfiguration;
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
@LoadBalancerClient(name = "HELLO-SERVICE")//, configuration = HelloServiceInstanceListSupplierConfig.class)
public class MyWebClientConfig
{
    /**
     * Siehe unten "Bugfix: ReactiveRegistrationClient".<br>
     * Ein Default-WebClient wird immer benötigt.
     *
     * @return WebClient.Builder
     */
    @Bean
    @Primary
    WebClient.Builder webClientBuilder()
    {
        // @formatter:off
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
                .doOnConnected(connection ->
                        connection
                                .addHandlerLast(new ReadTimeoutHandler(2, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(2, TimeUnit.SECONDS))
                )
                ;

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                ;
        // @formatter:on
    }

    /**
     * @return WebClient.Builder
     *
     * @see LoadBalancerClientConfiguration
     */
    @Bean
    @LoadBalanced
    WebClient.Builder webClientBuilderLoadBalanced()
    {
        // @formatter:off
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
                .doOnConnected(connection ->
                        connection
                                .addHandlerLast(new ReadTimeoutHandler(2, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(2, TimeUnit.SECONDS))
                )
                ;

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                ;
        // @formatter:on
    }

    //    /**
    //     * @see LoadBalancerClientConfiguration#reactorServiceInstanceLoadBalancer(Environment, LoadBalancerClientFactory)
    //     * @see MyServiceInstanceListSupplier
    //     */
    //    @Bean
    //    ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(Environment environment, LoadBalancerClientFactory loadBalancerClientFactory, ServiceInstanceListSupplier serviceInstanceListSupplier)
    //    {
    //        //        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
    //        String name = serviceInstanceListSupplier.getServiceId();
    //
    //        return new RandomLoadBalancer(loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
    //    }

    //    /**
    //     * Bugfix: ReactiveRegistrationClient von boot.admin funktioniert nicht mit LoadBalanced WebClient.
    //     *
    //     * @see de.codecentric.boot.admin.client.config.SpringBootAdminClientAutoConfiguration.ReactiveRegistrationClientConfig
    //     */
    //    @Bean
    //    public RegistrationClient registrationClient(ClientProperties client)
    //    {
    //        return new ReactiveRegistrationClient(WebClient.builder().build(), client.getReadTimeout());
    //    }
}

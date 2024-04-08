// Created: 14.02.2017
package de.freese.spring.ribbon;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import de.freese.spring.ribbon.myloadbalancer.LoadBalancer;
import de.freese.spring.ribbon.myloadbalancer.LoadBalancerInterceptor;
import de.freese.spring.ribbon.myloadbalancer.ping.LoadBalancerPingUrl;
import de.freese.spring.ribbon.myloadbalancer.strategy.LoadBalancerStrategy;
import de.freese.spring.ribbon.myloadbalancer.strategy.LoadBalancerStrategyRoundRobin;

/**
 * Demo mit eigenem LoadBalancer.
 *
 * @author Thomas Freese
 */
public class MyLoadBalancerApplication // implements RestTemplateCustomizer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MyLoadBalancerApplication.class);

    public static void main(final String[] args) throws Exception {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(MyLoadBalancerApplication.class)
                .profiles("my-loadbalancer")
                .run(args)) {
            final RestTemplate restTemplate = context.getBean("restTemplate", RestTemplate.class);
            final LoadBalancer loadBalancer = context.getBean("loadBalancer", LoadBalancer.class);

            final String server = loadBalancer.chooseServer("date-service");
            final URI serviceUri = URI.create(String.format("http://%s", server));
            LOGGER.info("manual look,up: {}", serviceUri);

            while (true) {
                final String result = restTemplate.getForObject("http://date-service/service/sysdate", String.class);

                LOGGER.info(result);
                // System.out.println(result);

                if (result == null) {
                    break;
                }

                TimeUnit.MILLISECONDS.sleep(3000);
            }
        }

        System.exit(0);
    }

    @Bean(destroyMethod = "shutdown")
    public LoadBalancer loadBalancer(final Environment env, final RestTemplate restTemplate) {
        final String serverList = env.getProperty("loadbalancer.servers");
        final String[] servers = serverList.split(",");

        final LoadBalancerPingUrl ping = new LoadBalancerPingUrl();
        ping.setPingAppendString("/service/ping"); // /netflix/service/actuator/health
        ping.setExpectedContent("true"); // UP
        ping.setHttpRequestFactory(restTemplate.getRequestFactory());
        // ping.setHttpRequestFactory(new Netty4ClientHttpRequestFactory());

        // final LoadBalancerPing ping = new LoadBalancerPingNoOp();

        final LoadBalancerStrategy strategy = new LoadBalancerStrategyRoundRobin();
        // final LoadBalancerStrategy strategy = new LoadBalancerStrategyFirstAvailable();

        final LoadBalancer loadBalancer = new LoadBalancer(servers);
        loadBalancer.setPingDelay(Long.parseLong(env.getProperty("loadbalancer.pingIntervall")));
        loadBalancer.setPing(ping);
        loadBalancer.setStrategy(strategy);

        final List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new LoadBalancerInterceptor(loadBalancer));

        restTemplate.setInterceptors(interceptors);

        return loadBalancer;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

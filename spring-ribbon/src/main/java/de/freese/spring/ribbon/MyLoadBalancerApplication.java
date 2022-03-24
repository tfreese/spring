// Created: 14.02.2017
package de.freese.spring.ribbon;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import de.freese.spring.ribbon.myloadbalancer.LoadBalancer;
import de.freese.spring.ribbon.myloadbalancer.LoadBalancerInterceptor;
import de.freese.spring.ribbon.myloadbalancer.ping.LoadBalancerPingUrl;
import de.freese.spring.ribbon.myloadbalancer.strategy.LoadBalancerStrategy;
import de.freese.spring.ribbon.myloadbalancer.strategy.LoadBalancerStrategyRoundRobin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

/**
 * Demo mit eigenem LoadBalancer.
 *
 * @author Thomas Freese
 */
public class MyLoadBalancerApplication // implements RestTemplateCustomizer
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MyLoadBalancerApplication.class);

    /**
     * @param args String[]
     *
     * @throws Exception Falls was schief geht.
     */
    public static void main(final String[] args) throws Exception
    {
        // @formatter:off
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(MyLoadBalancerApplication.class)
            .profiles("my-loadbalancer")
            .run(args))
        // @formatter:on
        {
            RestTemplate restTemplate = context.getBean("restTemplate", RestTemplate.class);
            LoadBalancer loadBalancer = context.getBean("loadBalancer", LoadBalancer.class);

            String server = loadBalancer.chooseServer("date-service");
            URI serviceUri = URI.create(String.format("http://%s", server));
            LOGGER.info("manual look,up: {}", serviceUri);

            while (true)
            {
                String result = restTemplate.getForObject("http://date-service/service/sysdate", String.class);

                LOGGER.info(result);
                // System.out.println(result);

                if (result == null)
                {
                    break;
                }

                Thread.sleep(3000);
            }
        }

        System.exit(0);
    }

    /**
     * @param env {@link Environment}
     * @param restTemplate {@link RestTemplate}
     *
     * @return {@link LoadBalancer}
     */
    @Bean(destroyMethod = "shutdown")
    public LoadBalancer loadBalancer(final Environment env, final RestTemplate restTemplate)
    {
        String serverList = env.getProperty("loadbalancer.servers");
        String[] servers = serverList.split("[,]");

        LoadBalancerPingUrl ping = new LoadBalancerPingUrl();
        ping.setPingAppendString("/service/ping"); // /netflix/service/actuator/health
        ping.setExpectedContent("true"); // UP
        ping.setHttpRequestFactory(restTemplate.getRequestFactory());
        // ping.setHttpRequestFactory(new Netty4ClientHttpRequestFactory());

        // LoadBalancerPing ping = new LoadBalancerPingNoOp();

        LoadBalancerStrategy strategy = new LoadBalancerStrategyRoundRobin();
        // LoadBalancerStrategy strategy = new LoadBalancerStrategyFirstAvailable();

        LoadBalancer loadBalancer = new LoadBalancer(servers);
        loadBalancer.setPingDelay(Long.parseLong(env.getProperty("loadbalancer.pingIntervall")));
        loadBalancer.setPing(ping);
        loadBalancer.setStrategy(strategy);

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new LoadBalancerInterceptor(loadBalancer));

        restTemplate.setInterceptors(interceptors);

        return loadBalancer;
    }

    /**
     * @return {@link RestTemplate}
     */
    @Bean
    public RestTemplate restTemplate()
    {
        return new RestTemplate();
    }
}

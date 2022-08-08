// Created: 14.02.2017
package de.freese.spring.ribbon;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * @author Thomas Freese
 */
@SpringBootApplication(exclude = GsonAutoConfiguration.class) // GSON hat Fehler verursacht -->
// (exclude =
// {
// EurekaClientAutoConfiguration.class, CommonsClientAutoConfiguration.class
// })
// @EnableDiscoveryClient(autoRegister = false)
@RibbonClient(name = "date-service", configuration = RibbonClientConfiguration.class)
public class RibbonClientWithoutEurekaApplication
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RibbonClientWithoutEurekaApplication.class);

    /**
     * @param args String[]
     *
     * @throws Exception Falls was schiefgeht.
     */
    public static void main(final String[] args) throws Exception
    {
        // Dependency darf nicht vorhanden sein: spring-cloud-starter-netflix-eureka-client

        // @formatter:off
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(RibbonClientWithoutEurekaApplication.class)
                .profiles("without-eureka")
            .run(args))
        // @formatter:on
        {
            RestTemplate restTemplate = context.getBean("restTemplate", RestTemplate.class);
            LoadBalancerClient loadBalancer = context.getBean("loadBalancerClient", LoadBalancerClient.class);

            ServiceInstance instance = loadBalancer.choose("date-service");
            URI serviceUri = URI.create(String.format("http://%s:%s", instance.getHost(), instance.getPort()));
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

                TimeUnit.MILLISECONDS.sleep(3000);
            }
        }

        System.exit(0);
    }

    /**
     * @return {@link RestTemplate}
     */
    @LoadBalanced
    @Bean
    public RestTemplate restTemplate()
    {
        return new RestTemplate();
    }
}

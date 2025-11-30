// Created: 14.02.2017
package de.freese.spring.ribbon;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Benötigt Dependency: spring-cloud-starter-netflix-eureka-client
 *
 * @author Thomas Freese
 */
@SpringBootApplication//(exclude = GsonAutoConfiguration.class)
// @EnableEurekaClient
@EnableDiscoveryClient
@RibbonClient(name = "date-service", configuration = RibbonClientConfiguration.class)
public class RibbonClientWithEurekaApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(RibbonClientWithEurekaApplication.class);

    static void main(final String[] args) throws Exception {
        // Benötigt Dependency: spring-cloud-starter-netflix-eureka-client

        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(RibbonClientWithEurekaApplication.class)
                .profiles("with-eureka")
                .run(args)) {
            final RestTemplate restTemplate = context.getBean("restTemplate", RestTemplate.class);

            while (true) {
                final String result = restTemplate.getForObject("http://DATE-SERVICE/service/sysdate", String.class);

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

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

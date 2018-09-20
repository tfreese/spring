// Created: 14.02.2017
package de.freese.j2ee.spring.hystrix;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConcurrentMapConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

/**
 * Startklasse des Hystrix-Clients.<br>
 * https://github.com/Netflix/Hystrix/tree/master/hystrix-contrib/hystrix-javanica<br>
 *
 * @author Thomas Freese
 */
@SpringBootApplication
@EnableHystrix
@EnableHystrixDashboard
@DefaultProperties(groupKey = "HystrixAppliction", commandProperties =
{
        @HystrixProperty(name = "execution.isolation.strategy", value = "THREAD"), // HystrixCommands im gleichen Thread ausführen
        @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "300")
})
public class HystrixApplication
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HystrixApplication.class);

    /**
     * @param args String[]
     * @throws Exception Falls was schief geht.
     */
    public static void main(final String[] args) throws Exception
    {
        // configuration from system properties
        ConcurrentMapConfiguration configFromSystemProperties = new ConcurrentMapConfiguration(new SystemConfiguration());

        // // configuration from local properties file
        ConcurrentMapConfiguration configFromPropertiesFile = new ConcurrentMapConfiguration(new PropertiesConfiguration("hystrix.properties"));

        // create a hierarchy of configuration that makes
        // 1) system properties override properties file
        ConcurrentCompositeConfiguration finalConfig = new ConcurrentCompositeConfiguration();
        finalConfig.addConfiguration(configFromSystemProperties, "systemConfig");
        finalConfig.addConfiguration(configFromPropertiesFile, "fileConfig");

        // install with ConfigurationManager so that finalConfig becomes the source of dynamic properties
        ConfigurationManager.install(finalConfig);

        // @formatter:off
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(HystrixApplication.class).run(args))
        // @formatter:on
        {
            HystrixApplication application = context.getBean(HystrixApplication.class);
            RestTemplate restTemplate = context.getBean(RestTemplate.class);

            String[] urls = new String[]
            {
                    "http://localhost:8081/service/sysdate", "http://localhost:8082/service/sysdate", "http://localhost:8083/service/sysdate"
            };

            while (true)
            {
                String result = application.getSysdate1(restTemplate, urls);

                LOGGER.info(result);
                // System.out.println(result);

                if (result == null)
                {
                    break;
                }

                Thread.sleep(2000);
            }
        }

        System.exit(0);
    }

    /**
     * Erzeugt eine neue Instanz von {@link HystrixApplication}
     */
    public HystrixApplication()
    {
        super();
    }

    /**
     * @param restTemplate {@link RestTemplate}
     * @param urls String[]
     * @return String
     */
    @HystrixCommand(commandKey = "getSysdate1", threadPoolKey = "sysDate", fallbackMethod = "getSysdate2")
    public String getSysdate1(final RestTemplate restTemplate, final String[] urls)
    {
        LOGGER.info("getSysdate1");

        String result = restTemplate.getForObject(urls[0], String.class);
        LOGGER.info(result);

        return result;
    }

    /**
     * @param restTemplate {@link RestTemplate}
     * @param urls String[]
     * @return String
     */
    @HystrixCommand(threadPoolKey = "sysDate", fallbackMethod = "getSysdate3")
    public String getSysdate2(final RestTemplate restTemplate, final String[] urls)
    {
        LOGGER.info("getSysdate2");

        String result = restTemplate.getForObject(urls[1], String.class);
        LOGGER.info(result);

        return result;
    }

    /**
     * @param restTemplate {@link RestTemplate}
     * @param urls String[]
     * @return String
     */
    @HystrixCommand(threadPoolKey = "sysDate", fallbackMethod = "getSysdateFallback")
    public String getSysdate3(final RestTemplate restTemplate, final String[] urls)
    {
        LOGGER.info("getSysdate3");

        String result = restTemplate.getForObject(urls[2], String.class);
        LOGGER.info(result);

        return result;
    }

    /**
     * @param restTemplate {@link RestTemplate}
     * @param urls String[]
     * @return String
     */
    @HystrixCommand
    public String getSysdateFallback(final RestTemplate restTemplate, final String[] urls)
    {
        String result = "fallback";
        LOGGER.info(result);

        return result;
    }

    /**
     * @return {@link RestTemplate}
     */
    @Bean
    public RestTemplate restTemplate()
    {
        RestTemplateBuilder builder = new RestTemplateBuilder();

        return builder.build();
    }
}

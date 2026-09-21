package de.freese.spring.hystrix;

import java.util.concurrent.TimeUnit;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConcurrentMapConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

/**
 * <a href="https://github.com/Netflix/Hystrix/tree/master/hystrix-contrib/hystrix-javanica">hystrix-javanica</a><br>
 *
 * @author Thomas Freese
 * @since 14.02.2017
 */
@SpringBootApplication
@EnableHystrix
@EnableHystrixDashboard

@DefaultProperties(groupKey = "HystrixApplication") // Other Configuration sees hystrix.properties

// @DefaultProperties(groupKey = "HystrixApplication", commandProperties = {
// @HystrixProperty(name = "execution.isolation.strategy", value = "THREAD"), // HystrixCommands in separaten Threads ausführen
// @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "300")
// })
public class HystrixApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(HystrixApplication.class);

    static void main(final String[] args) throws Exception {
        // Configuration from environment properties.
        final ConcurrentMapConfiguration configFromEnvironmentProperties = new ConcurrentMapConfiguration(new EnvironmentConfiguration());

        // Configuration from system properties.
        final ConcurrentMapConfiguration configFromSystemProperties = new ConcurrentMapConfiguration(new SystemConfiguration());

        // Configuration from a local properties file.
        final ConcurrentMapConfiguration configFromPropertiesFile = new ConcurrentMapConfiguration(new PropertiesConfiguration("hystrix.properties"));

        // Create a hierarchy of configuration that makes.
        // 1) system properties override properties file
        final ConcurrentCompositeConfiguration finalConfig = new ConcurrentCompositeConfiguration();
        finalConfig.addConfiguration(configFromEnvironmentProperties, "environmentConfig");
        finalConfig.addConfiguration(configFromSystemProperties, "systemConfig");
        finalConfig.addConfiguration(configFromPropertiesFile, "fileConfig");

        // Install with ConfigurationManager so that finalConfig becomes the source of dynamic properties.
        ConfigurationManager.install(finalConfig);

        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(HystrixApplication.class).run(args)) {
            final HystrixApplication application = context.getBean(HystrixApplication.class);
            final RestClient restClient = context.getBean(RestClient.class);

            final String[] urls = new String[]{"http://localhost:8081/service/sysdate", "http://localhost:8082/service/sysdate", "http://localhost:8083/service/sysdate"};

            while (true) {
                final String result = application.getSysdate1(restClient, urls);

                LOGGER.info(result);
                // System.out.println(result);

                if (result == null) {
                    break;
                }

                TimeUnit.MILLISECONDS.sleep(2000L);
            }
        }

        System.exit(0);
    }

    @HystrixCommand(commandKey = "getSysdate1", threadPoolKey = "sysDate", fallbackMethod = "getSysdate2")
    public String getSysdate1(final RestClient restClient, final String[] urls) {
        LOGGER.info("getSysdate1");

        final String result = restClient.get().uri(urls[0]).retrieve().toEntity(String.class).getBody();
        LOGGER.info(result);

        return result;
    }

    @HystrixCommand(threadPoolKey = "sysDate", fallbackMethod = "getSysdate3")
    public String getSysdate2(final RestClient restClient, final String[] urls) {
        LOGGER.info("getSysdate2");

        final String result = restClient.get().uri(urls[1]).retrieve().toEntity(String.class).getBody();
        LOGGER.info(result);

        return result;
    }

    @HystrixCommand(threadPoolKey = "sysDate", fallbackMethod = "getSysdateFallback")
    public String getSysdate3(final RestClient restClient, final String[] urls) {
        LOGGER.info("getSysdate3");

        final String result = restClient.get().uri(urls[2]).retrieve().toEntity(String.class).getBody();
        LOGGER.info(result);

        return result;
    }

    @HystrixCommand(commandProperties = {
            // Im aktuellen Thread ausführen.
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")})
    public String getSysdateFallback(final RestClient restClient, final String[] urls) {
        final String result = "fallback";
        LOGGER.info(result);

        return result;
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }
}

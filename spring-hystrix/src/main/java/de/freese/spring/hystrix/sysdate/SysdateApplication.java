// Created: 01.03.2017
package de.freese.spring.hystrix.sysdate;

import java.util.concurrent.TimeUnit;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConcurrentMapConfiguration;
import com.netflix.config.ConfigurationManager;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

/**
 * @author Thomas Freese
 */
public class SysdateApplication
{
    public static void main(final String[] args) throws Exception
    {
        // configuration from environment properties
        ConcurrentMapConfiguration configFromEnvironmentProperties = new ConcurrentMapConfiguration(new EnvironmentConfiguration());

        // configuration from system properties
        ConcurrentMapConfiguration configFromSystemProperties = new ConcurrentMapConfiguration(new SystemConfiguration());

        // // configuration from local properties file
        ConcurrentMapConfiguration configFromPropertiesFile = new ConcurrentMapConfiguration(new PropertiesConfiguration("hystrix.properties"));

        // create a hierarchy of configuration that makes
        // 1) system properties override properties file
        ConcurrentCompositeConfiguration finalConfig = new ConcurrentCompositeConfiguration();
        finalConfig.addConfiguration(configFromEnvironmentProperties, "environmentConfig");
        finalConfig.addConfiguration(configFromSystemProperties, "systemConfig");
        finalConfig.addConfiguration(configFromPropertiesFile, "fileConfig");

        // install with ConfigurationManager so that finalConfig becomes the source of dynamic properties
        ConfigurationManager.install(finalConfig);

        // Server1.main(new String[0]);
        // Server2.main(new String[0]);
        // Server3.main(new String[0]);

        RestTemplate restTemplate = new RestTemplateBuilder().build();
        String[] urls = new String[]
                {
                        "http://localhost:8081/service/sysdate/", "http://localhost:8082/service/sysdate/", "http://localhost:8083/service/sysdate/"
                };
        // System.out.println(restTemplate.getForObject("http://localhost:8081/service/sysdate/", String.class));

        while (true)
        {
            SysDateHystrixCommand cmd = new SysDateHystrixCommand();
            cmd.setRestTemplate(restTemplate);
            cmd.setURLs(urls);

            String result = cmd.execute();

            // System.out.println(result);

            if (result == null)
            {
                break;
            }

            TimeUnit.MILLISECONDS.sleep(1000);
        }

        System.exit(0);
    }
}

// Created: 01.03.2017
package de.freese.j2ee.spring.hystrix.primarysecondary;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.LoggerFactory;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConcurrentMapConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * @author Thomas Freese
 */
public class PrimarySecondaryApplication
{
    /**
     * @param args String[]
     * @throws Exception Falls was schief geht.
     */
    public static void main(final String[] args) throws Exception
    {
        Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        // configuration from system properties
        ConcurrentMapConfiguration configFromSystemProperties = new ConcurrentMapConfiguration(new SystemConfiguration());

        // // configuration from local properties file
        ConcurrentMapConfiguration configFromPropertiesFile = new ConcurrentMapConfiguration(
                new PropertiesConfiguration("hystrix.properties"));

        // create a hierarchy of configuration that makes
        // 1) system properties override properties file
        ConcurrentCompositeConfiguration finalConfig = new ConcurrentCompositeConfiguration();
        finalConfig.addConfiguration(configFromSystemProperties, "systemConfig");
        finalConfig.addConfiguration(configFromPropertiesFile, "fileConfig");

        // install with ConfigurationManager so that finalConfig becomes the source of dynamic properties
        ConfigurationManager.install(finalConfig);

        try (HystrixRequestContext context = HystrixRequestContext.initializeContext())
        {
            for (int i = 0; i < 2; i++)
            {
                for (int j = 1; j <= 10; j++)
                {
                    PrimarySecondaryCommand cmd = new PrimarySecondaryCommand(j);

                    String result = cmd.execute();

                    // Ohne Logs der Commands wird das Ergebnis aus dem Cache geholt, siehe PrimarySecondaryCommand#getCacheKey.
                    System.out.println(result);

                    // if (cmd.isResponseFromCache())
                    // {
                    // break;
                    // }

                    if ((j % 5) == 0)
                    {
                        // Auf Secondary umschalten.
                        ConfigurationManager.getConfigInstance().setProperty("primarySecondary.usePrimary", false);
                    }

                    Thread.sleep(1000);
                }

                System.out.println();
            }
        }

        ConfigurationManager.getConfigInstance().clear();

        System.exit(0);
    }
}

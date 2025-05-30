// Created: 01.03.2017
package de.freese.spring.hystrix.primarysecondary;

import java.util.concurrent.TimeUnit;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConcurrentMapConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Freese
 */
public final class PrimarySecondaryApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrimarySecondaryApplication.class);

    public static void main(final String[] args) throws Exception {
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

        try (HystrixRequestContext context = HystrixRequestContext.initializeContext()) {
            for (int i = 0; i < 2; i++) {
                for (int j = 1; j <= 10; j++) {
                    final PrimarySecondaryCommand cmd = new PrimarySecondaryCommand(j);

                    final String result = cmd.execute();

                    // Without Logs of the Commands the Result is getting from Cache, see PrimarySecondaryCommand#getCacheKey.
                    LOGGER.info(result);

                    // if (cmd.isResponseFromCache()) {
                    // break;
                    // }

                    if ((j % 5) == 0) {
                        // Switch to Secondary.
                        ConfigurationManager.getConfigInstance().setProperty("primarySecondary.usePrimary", false);
                    }

                    TimeUnit.MILLISECONDS.sleep(1000);
                }
            }

            context.shutdown();
        }

        ConfigurationManager.getConfigInstance().clear();

        System.exit(0);
    }

    private PrimarySecondaryApplication() {
        super();
    }
}

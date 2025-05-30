package de.freese.spring.resilience;

import java.text.NumberFormat;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <a href="https://github.com/spring-cloud/spring-cloud-circuitbreaker">spring-cloud-circuitbreaker</a><br>
 * for i in {1..10}; do curl localhost:8080/greet?name=$i; echo ""; done;
 *
 * @author Thomas Freese
 */
@SpringBootApplication
public class ResilienceApplication implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResilienceApplication.class);

    public static void main(final String[] args) {
        SpringApplication.run(ResilienceApplication.class, args);
    }

    @Override
    public void run(final ApplicationArguments args) {
        final Runtime runtime = Runtime.getRuntime();

        final NumberFormat format = NumberFormat.getInstance();

        final long maxMemory = runtime.maxMemory();
        final long allocatedMemory = runtime.totalMemory();
        final long freeMemory = runtime.freeMemory();
        final long divider = 1024L * 1024L;
        final String unit = "MB";

        LOGGER.info("========================== System Info ==========================");
        LOGGER.info("System: {}/{} {}", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version"));
        LOGGER.info("User-Dir: {}", System.getProperty("user.dir"));
        LOGGER.info("Programm-Args: {}", Arrays.asList(args.getSourceArgs()));
        LOGGER.info("CPU Cores: {}", runtime.availableProcessors());
        LOGGER.info("Free memory: {}", format.format(freeMemory / divider) + unit);
        LOGGER.info("Allocated memory: {}", format.format(allocatedMemory / divider) + unit);
        LOGGER.info("Max memory: {}", format.format(maxMemory / divider) + unit);
        LOGGER.info("Total free memory: {}", format.format((freeMemory + (maxMemory - allocatedMemory)) / divider) + unit);
        LOGGER.info("=================================================================");
    }
}

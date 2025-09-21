// Created: 28.04.2022
package de.freese.spring.atomicos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
public final class AtomicosApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(AtomicosApplication.class);

    static void main(final String[] args) {
        // SpringApplication.run(AtomicosApplication.class, args);
        final SpringApplication application = new SpringApplication(AtomicosApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        final ConfigurableApplicationContext applicationContext = application.run(args);

        final TxService service = applicationContext.getBean(TxService.class);

        try {
            service.insertData("Person1", "City1");
            service.insertData("Person2", "City2");
            service.selectAll();
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        try {
            service.insertData("Person3", "EinVielZuLangerStadtName");
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }

        try {
            service.selectAll();
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private AtomicosApplication() {
        super();
    }
}

// Created: 25.02.2026
package de.spring.jooq;

import org.jooq.codegen.GenerationTool;
import org.jooq.meta.h2.H2Database;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Logging;
import org.jooq.meta.jaxb.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
@Order(1)
@Profile("!test")
public class GeneratorRunner implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorRunner.class);

    @Value("${spring.datasource.driver-class-name:none}")
    private String driver;

    @Value("${spring.datasource.password:none}")
    private String password;

    @Value("${spring.datasource.url:none}")
    private String url;

    @Value("${spring.datasource.username:none}")
    private String username;

    @Override
    public void run(final String... args) throws Exception {
        LOGGER.info("Running GeneratorRunner...");

        // generateModel();
    }

    private void generateModel() throws Exception {
        final Configuration configuration = new Configuration()
                .withLogging(Logging.DEBUG)
                .withJdbc(new Jdbc()
                        .withDriver(driver)
                        .withUrl(url)
                        .withUser(username)
                        .withPassword(password)
                )
                .withGenerator(new Generator()
                        .withName("org.jooq.codegen.JavaGenerator")
                        .withDatabase(new Database()
                                .withName(H2Database.class.getName())
                                .withIncludes(".*")
                                .withExcludes("")
                                .withInputSchema("PUBLIC")
                                .withIncludeSequences(true)
                                // .withIncludeSystemSequences(true)
                                .withIncludeIndexes(false)
                                .withIncludeForeignKeys(false)
                                .withIncludeUniqueKeys(false)
                        )
                        .withGenerate(new Generate()
                                .withComments(true)
                                .withDefaultCatalog(false)
                                .withDefaultSchema(false)
                                .withKeys(false)
                                .withSequences(true))
                        .withTarget(new Target()
                                .withPackageName("de.spring.jooq.model")
                                .withDirectory("spring-jooq/src/main/java")
                                .withEncoding("UTF-8")
                                .withLocale("de")
                                .withClean(true)
                        )
                );

        GenerationTool.generate(configuration);
    }
}

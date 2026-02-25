// Created: 25.02.2026
package de.spring.jooq;

import org.jooq.conf.RenderQuotedNames;
import org.springframework.boot.jooq.autoconfigure.DefaultConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Thomas Freese
 */
@Configuration
public class Config {
    // DSLContext dslContext = DSL.using(dataSource, SQLDialect.H2, new Settings());
    // final DSLContext dslContext = new DefaultDSLContext(new DefaultConfiguration()
    //         .set(SQLDialect.H2)
    //         //.set(dataSource)
    //         .set(new Settings())
    // );

    @Bean
    public DefaultConfigurationCustomizer jooqDefaultConfigurationCustomizer() {
        return config -> config.settings()
                .withRenderSchema(false)
                .withRenderQuotedNames(RenderQuotedNames.NEVER)
                ;
    }
}

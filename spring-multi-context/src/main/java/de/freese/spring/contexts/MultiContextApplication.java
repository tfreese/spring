// Created: 25.04.2025
package de.freese.spring.contexts;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import de.freese.spring.contexts.config.Child1Config;
import de.freese.spring.contexts.config.Child2Config;
import de.freese.spring.contexts.config.ParentConfig;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
public final class MultiContextApplication {
    static void main(final String[] args) {
        // SpringApplication.run(MainApplication.class, args);

        // Parent <- Child1 <- Child2 (Chain)
        // new SpringApplicationBuilder()
        //         .properties("spring.config.name=application-profiles")
        //         .sources(ParentConfig.class).profiles("parent").web(WebApplicationType.NONE)
        //         .child(Child1Config.class).profiles("child1").web(WebApplicationType.NONE)
        //         .child(Child2Config.class).profiles("child2").web(WebApplicationType.NONE)
        //         .run(args);

        // Parent <- Child1,Child2 (Hierarchie)
        final SpringApplicationBuilder parentBuilder = new SpringApplicationBuilder()
                .sources(ParentConfig.class)
                .properties("spring.config.name=application-parent")
                .web(WebApplicationType.NONE);

        final SpringApplicationBuilder child1Builder = new SpringApplicationBuilder()
                .sources(Child1Config.class)
                .properties("spring.config.name=application-child1")
                .web(WebApplicationType.NONE);

        final SpringApplicationBuilder child2Builder = new SpringApplicationBuilder()
                .sources(Child2Config.class)
                .properties("spring.config.name=application-child2")
                .web(WebApplicationType.NONE);

        final ConfigurableApplicationContext parentContext = parentBuilder.run(args);
        child1Builder.parent(parentContext).run(args);
        child2Builder.parent(parentContext).run(args);
    }

    private MultiContextApplication() {
        super();
    }
}

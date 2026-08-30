package de.freese.spring.contexts.child1;

import java.util.Objects;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import de.freese.spring.contexts.parent.ParentBean;

/**
 * @author Thomas Freese
 * @since 25.04.2025
 */
@Component
public class Child1Runner implements ApplicationRunner, ApplicationContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(Child1Runner.class);

    private final Child1Bean child1Bean;
    private final ParentBean parentBean;

    private ApplicationContext applicationContext;

    public Child1Runner(final ParentBean parentBean, final Child1Bean child1Bean) {
        super();

        this.parentBean = Objects.requireNonNull(parentBean, "parentBean required");
        this.child1Bean = Objects.requireNonNull(child1Bean, "child1Bean required");
    }

    @Override
    public void run(final @NonNull ApplicationArguments args) {
        LOGGER.info("{} - {} - {}", parentBean, child1Bean, applicationContext.containsBean("child2Bean"));
    }

    @Override
    public void setApplicationContext(final @NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext required");
    }
}

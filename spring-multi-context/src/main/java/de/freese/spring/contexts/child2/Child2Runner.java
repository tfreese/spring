// Created: 25.04.2025
package de.freese.spring.contexts.child2;

import java.util.Objects;

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
 */
@Component
public class Child2Runner implements ApplicationRunner, ApplicationContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(Child2Runner.class);

    private final Child2Bean child2Bean;
    private final ParentBean parentBean;

    private ApplicationContext applicationContext;

    public Child2Runner(final ParentBean parentBean, final Child2Bean child2Bean) {
        super();

        this.parentBean = Objects.requireNonNull(parentBean, "parentBean required");
        this.child2Bean = Objects.requireNonNull(child2Bean, "child2Bean required");
    }

    @Override
    public void run(final ApplicationArguments args) {
        LOGGER.info("{} - {} - {}", parentBean, applicationContext.containsBean("child1Bean"), child2Bean);
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext required");
    }
}

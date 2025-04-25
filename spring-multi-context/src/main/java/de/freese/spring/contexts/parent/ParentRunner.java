// Created: 25.04.2025
package de.freese.spring.contexts.parent;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
public class ParentRunner implements ApplicationRunner, ApplicationContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParentRunner.class);

    private final ParentBean parentBean;

    private ApplicationContext applicationContext;

    public ParentRunner(final ParentBean parentBean) {
        super();

        this.parentBean = Objects.requireNonNull(parentBean, "parentBean required");
    }

    @Override
    public void run(final ApplicationArguments args) {
        LOGGER.info("{} - {} - {}", parentBean, applicationContext.containsBean("child1Bean"), applicationContext.containsBean("child2Bean"));
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext required");
    }
}

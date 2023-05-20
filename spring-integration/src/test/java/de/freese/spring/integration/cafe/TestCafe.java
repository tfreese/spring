// Created: 14.04.2012
package de.freese.spring.integration.cafe;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <a href="https://github.com/spring-projects/spring-integration-samples">spring-integration-samples</a><br>
 * <a href="https://github.com/spring-projects/spring-integration-samples/tree/master/applications/cafe/cafe-si">cafe-si</a>
 *
 * @author Thomas Freese
 */
class TestCafe {
    // static void main(final String[] args) throws Exception
    // {
    // TestCafe testCafe = new TestCafe();
    //
    // testCafe.testCafeDemoWithXmlSupport();
    // testCafe.testCafeDemoWithAnnotationSupport();
    // testCafe.testCafeConfig();
    // testCafe.testCafeDsl();
    // }

    @Test
    void testCafeConfig() throws Exception {
        //@formatter:off
        try(ConfigurableApplicationContext context= new SpringApplicationBuilder()
            .headless(true)
            .registerShutdownHook(true)
            .web(WebApplicationType.NONE)
            .bannerMode(Banner.Mode.OFF)
            .sources(de.freese.spring.integration.cafe.config.Application.class)
            .build()
            .run())
        //@formatter:on
        {
            testCafe(context);
        }
    }

    @Test
    void testCafeDemoWithAnnotationSupport() throws Exception {
        try (AbstractApplicationContext context = new ClassPathXmlApplicationContext("cafeDemo-annotation.xml")) {
            context.registerShutdownHook();

            testCafe(context);
        }
    }

    @Test
    void testCafeDemoWithXmlSupport() throws Exception {
        try (AbstractApplicationContext context = new ClassPathXmlApplicationContext("cafeDemo-xml.xml")) {
            context.registerShutdownHook();

            testCafe(context);
        }
    }

    @Test
    void testCafeDsl() throws Exception {
        // ConfigurableApplicationContext context = SpringApplication.run(Application.class);

        //@formatter:off
        try(ConfigurableApplicationContext context= new SpringApplicationBuilder()
            .headless(true)
            .registerShutdownHook(true)
            .web(WebApplicationType.NONE)
            .bannerMode(Banner.Mode.OFF)
            .sources(de.freese.spring.integration.cafe.dsl.Application.class)
            .build()
            .run())
        //@formatter:on
        {
            testCafe(context);
        }
    }

    private void testCafe(final ApplicationContext context) throws Exception {
        Cafe cafe = context.getBean(Cafe.class);

        for (int i = 1; i < 4; i++) {
            Order order = new Order(i);
            order.addItem(DrinkType.LATTE, false);
            order.addItem(DrinkType.MOCHA, true);
            cafe.placeOrder(order);
        }

        // Zeit für Arbeit des Spring-Frameworks.
        TimeUnit.MILLISECONDS.sleep(5000);
    }
}

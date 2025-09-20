// Created: 14.04.2012
package de.freese.spring.integration.cafe;

import static org.awaitility.Awaitility.await;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.freese.spring.integration.cafe.config.ApplicationConfig;
import de.freese.spring.integration.cafe.dsl.ApplicationDsl;

/**
 * <a href="https://github.com/spring-projects/spring-integration-samples">spring-integration-samples</a><br>
 * <a href="https://github.com/spring-projects/spring-integration-samples/tree/master/applications/cafe/cafe-si">cafe-si</a>
 *
 * @author Thomas Freese
 */
class TestCafe {
    // static void main() throws Exception {
    // TestCafe testCafe = new TestCafe();
    //
    // testCafe.testCafeDemoWithXmlSupport();
    // testCafe.testCafeDemoWithAnnotationSupport();
    // testCafe.testCafeConfig();
    // testCafe.testCafeDsl();
    // }

    @Test
    void testCafeConfig() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder()
                .headless(true)
                .registerShutdownHook(true)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                .sources(ApplicationConfig.class)
                .build()
                .run()) {
            testCafe(context);
        }
    }

    @Test
    void testCafeDemoWithAnnotationSupport() {
        try (AbstractApplicationContext context = new ClassPathXmlApplicationContext("cafeDemo-annotation.xml")) {
            context.registerShutdownHook();

            testCafe(context);
        }
    }

    @Test
    void testCafeDemoWithXmlSupport() {
        try (AbstractApplicationContext context = new ClassPathXmlApplicationContext("cafeDemo-xml.xml")) {
            context.registerShutdownHook();

            testCafe(context);
        }
    }

    @Test
    void testCafeDsl() {
        // ConfigurableApplicationContext context = SpringApplication.run(ApplicationConfig.class);

        try (ConfigurableApplicationContext context = new SpringApplicationBuilder()
                .headless(true)
                .registerShutdownHook(true)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                .sources(ApplicationDsl.class)
                .build()
                .run()) {
            testCafe(context);
        }
    }

    private void testCafe(final ApplicationContext context) {
        final Cafe cafe = context.getBean(Cafe.class);

        for (int i = 1; i < 4; i++) {
            final Order order = new Order(i);
            order.addItem(DrinkType.LATTE, false);
            order.addItem(DrinkType.MOCHA, true);
            cafe.placeOrder(order);
        }

        // Some extra Time for shutdown.
        await().pollDelay(Duration.ofMillis(5000L)).until(() -> true);
    }
}

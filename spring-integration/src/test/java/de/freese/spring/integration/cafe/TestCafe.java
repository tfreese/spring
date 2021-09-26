// Created: 14.04.2012
package de.freese.spring.integration.cafe;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * https://github.com/spring-projects/spring-integration-samples<br>
 * https://github.com/spring-projects/spring-integration-samples/tree/master/applications/cafe/cafe-si
 *
 * @author Thomas Freese
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class TestCafe
{

    // /**
    // * @param args String[]
    // * @throws Exception Falls was schief geht.
    // */
    // static void main(final String[] args) throws Exception
    // {
    // TestCafe testCafe = new TestCafe();
    //
    // testCafe.test010CafeDemoWithXmlSupport();
    // testCafe.test020CafeDemoWithAnnotationSupport();
    // testCafe.test030CafeConfig();
    // testCafe.test040CafeDsl();
    // }

    /**
     * @param context {@link ApplicationContext}
     *
     * @throws Exception Falls was schief geht.
     */
    private void testCafe(final ApplicationContext context) throws Exception
    {
        Cafe cafe = context.getBean(Cafe.class);

        for (int i = 1; i < 4; i++)
        {
            Order order = new Order(i);
            order.addItem(DrinkType.LATTE, false);
            order.addItem(DrinkType.MOCHA, true);
            cafe.placeOrder(order);
        }

        // Zeit für Arbeit des Springframeworks.
        TimeUnit.MILLISECONDS.sleep(5000);
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testCafeConfig() throws Exception
    {
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

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testCafeDemoWithAnnotationSupport() throws Exception
    {
        try (AbstractApplicationContext context = new ClassPathXmlApplicationContext("cafeDemo-annotation.xml"))
        {
            context.registerShutdownHook();

            testCafe(context);
        }
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testCafeDemoWithXmlSupport() throws Exception
    {
        try (AbstractApplicationContext context = new ClassPathXmlApplicationContext("cafeDemo-xml.xml"))
        {
            context.registerShutdownHook();

            testCafe(context);
        }
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testCafeDsl() throws Exception
    {
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
}

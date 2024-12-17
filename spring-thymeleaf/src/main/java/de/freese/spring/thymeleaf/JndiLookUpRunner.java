// Created: 12.09.2018
package de.freese.spring.thymeleaf;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
@Profile("!test")
@Order(10)
public class JndiLookUpRunner implements ApplicationRunner {
    public static final Logger LOGGER = LoggerFactory.getLogger(JndiLookUpRunner.class);

    private static void iterate(final Context context) {
        try {
            final NamingEnumeration<NameClassPair> enumeration = context.list("");
            // final NamingEnumeration<NameClassPair> enumeration = context.list("java:comp/env");

            while (enumeration.hasMoreElements()) {
                final NameClassPair nameClassPair = enumeration.nextElement();
                LOGGER.info("{}", nameClassPair);
            }

            enumeration.close();
        }
        catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage());
        }
    }

    private static void lookup(final Context context) {
        try {
            final Object object = context.lookup("test");
            LOGGER.info("{}", object);
        }
        catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage());
        }
    }

    @Override
    public void run(final ApplicationArguments args) throws Exception {
        LOGGER.info("JNDI Content");

        try {
            // TomcatServletWebServerFactory#getTomcatWebServer
            // Tomcat#enableNaming
            // System.setProperty("catalina.useNaming", "true");
            // System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, "org.apache.naming");
            // System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");

            // final Context context = (javax.naming.Context) initialContext.lookup("java:comp/env");
            final Context context = new InitialContext();

            iterate(context);
            lookup(context);

            final JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
            bean.setJndiName("java:/comp/env/test");
            bean.afterPropertiesSet();
            final Object object = bean.getObject();

            LOGGER.info("{}", object);

            context.close();
        }
        catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage());
        }
    }
}

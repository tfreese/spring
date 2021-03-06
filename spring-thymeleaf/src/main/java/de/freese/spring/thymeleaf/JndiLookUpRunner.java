/**
 * Created: 12.09.2018
 */
package de.freese.spring.thymeleaf;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
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
public class JndiLookUpRunner implements CommandLineRunner
{
    /**
    *
    */
    public static final Logger LOGGER = LoggerFactory.getLogger(JndiLookUpRunner.class);

    /**
     * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])
     */
    @Override
    public void run(final String...args) throws Exception
    {
        LOGGER.info("");

        try
        {
            System.out.println();
            System.out.println("JNDI Content");

            // TomcatServletWebServerFactory#getTomcatWebServer
            // Tomcat#enableNaming
            // System.setProperty("catalina.useNaming", "true");
            // System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, "org.apache.naming");
            // System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");

            InitialContext initialContext = new InitialContext();
            Context context = initialContext;

            try
            {
                // Context context = (javax.naming.Context) initialContext.lookup("java:comp/env");

                // NamingEnumeration<NameClassPair> enumeration = context.list("");
                NamingEnumeration<NameClassPair> enumeration = context.list("java:comp");

                while (enumeration.hasMoreElements())
                {
                    NameClassPair nameClassPair = enumeration.nextElement();
                    System.out.println(nameClassPair);
                }
            }
            catch (Exception ex)
            {
                LOGGER.error(ex.getLocalizedMessage());
            }

            Object object = null;

            try
            {
                object = context.lookup("test");
                System.out.println(object);
            }
            catch (Exception ex)
            {
                LOGGER.error(ex.getLocalizedMessage());
            }

            JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
            bean.setJndiName("java:/comp/env/test");
            bean.afterPropertiesSet();
            object = bean.getObject();

            System.out.println(object);

            // context.close();
            // initialContext.close();
        }
        catch (Exception ex)
        {
            LOGGER.error(ex.getLocalizedMessage());
        }
    }
}

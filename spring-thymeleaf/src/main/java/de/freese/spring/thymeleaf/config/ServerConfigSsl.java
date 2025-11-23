// Created: 11.09.2018
package de.freese.spring.thymeleaf.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.tomcat.TomcatWebServer;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.servlet.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author Thomas Freese
 */
@Configuration
@Profile("with-ssl")
public class ServerConfigSsl {
    // @Value("${server.port ?: #{systemProperties['server_port']}}")
    // @Value("${server.port : #{systemProperties['server_port']}}")
    // @Value("${server.port : #{systemProperties.server_port}}")
    // @Value("${local.server.port}")
    // @Value("${server.port:8443}")
    @Value("${server.port}")
    private int serverPort;

    @Bean
    public ServletWebServerFactory servletContainer() {
        final TomcatServletWebServerFactory tomcatServletWebServerFactory = new TomcatServletWebServerFactory() {
            @Override
            protected TomcatWebServer getTomcatWebServer(final Tomcat tomcat) {
                // Enable JNDI.
                tomcat.enableNaming();

                return super.getTomcatWebServer(tomcat);
            }

            @Override
            protected void postProcessContext(final Context context) {
                // Setup SSL Context.
                final SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");

                final SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);

                // JNDI-Content
                final ContextEnvironment contextEnvironment = new ContextEnvironment();
                contextEnvironment.setName("test");
                contextEnvironment.setType("java.lang.String");
                contextEnvironment.setValue("MY-JNDI");
                contextEnvironment.setOverride(false);
                context.getNamingResources().addEnvironment(contextEnvironment);

                // ContextResource works only for "javax.sql.DataSource" und "javax.mail.Session".
                // See org.apache.naming.factory.ResourceFactory.
                // final ContextResource resource = new ContextResource();
                // resource.setName("jdbc/datasource");
                // // resource.setType("org.hsql.jdbcDriver");
                // resource.setType(jdbcDriver.class.getName());
                // resource.setSingleton(true);
                // resource.setType(DataSource.class.getName());
                // resource.setProperty("driverClassName", "oracle.jdbc.driver.OracleDriver");
                // resource.setProperty("url", "DBURL");
                // resource.setProperty("username", "DBUSER");
                // resource.setProperty("password", "PASSWORD");
                // context.getNamingResources().addResource(resource);

                // final ContextResourceEnvRef envRef = new ContextResourceEnvRef();
                // envRef.setName("test");
                // envRef.setType(DataSourceProperties.class.getName());
                // context.getNamingResources().addResourceEnvRef(envRef);

                // final ContextResourceLink resourceLink = new ContextResourceLink();
                // resourceLink.setName("jdbc/aJNDI");
                // resourceLink.setGlobal("jdbc/aJNDIGlobal");
                // resourceLink.setType("javax.sql.DataSource");
                // resourceLink.setProperty("auth", "Container");
                //
                // context.getNamingResources().addResourceLink(resourceLink);

                // logger.info(context.getNamingResources());
                LoggerFactory.getLogger(getClass()).info("{}", context.getNamingResources());
            }
        };

        tomcatServletWebServerFactory.addAdditionalConnectors(servletRedirectConnector());

        return tomcatServletWebServerFactory;
    }

    /**
     * Redirect Port 9090 to 8443.
     */
    private Connector servletRedirectConnector() {
        final Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(9090);
        connector.setSecure(false);
        connector.setRedirectPort(serverPort);

        return connector;
    }
}

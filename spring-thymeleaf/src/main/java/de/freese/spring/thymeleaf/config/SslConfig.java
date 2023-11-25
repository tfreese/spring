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
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author Thomas Freese
 */
@Configuration
@Profile("with-ssl")
public class SslConfig {
    // @Value("${server.port ?: #{systemProperties['server_port']}}")
    // @Value("${server.port : #{systemProperties['server_port']}}")
    // @Value("${server.port : #{systemProperties.server_port}}")
    // @Value("${local.server.port}")
    // @Value("${server.port:8443}")
    @Value("${server.port}")
    private int serverPort;

    // /**
    // * @param sslContext {@link SSLContext}
    // * @return {@link HttpComponentsClientHttpRequestFactory}
    // * @throws Exception Falls was schief geht.
    // */
    // @SuppressWarnings("resource")
    // public HttpComponentsClientHttpRequestFactory createHttpComponentsClientHttpRequestFactory(final SSLContext sslContext) throws Exception
    // {
    // SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
    //
    // CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(sslsf).build();
    //
    // HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(client);
    // httpRequestFactory.setReadTimeout(3000);
    // httpRequestFactory.setConnectTimeout(3000);
    //
    // // this.restTemplateBuilder = this.restTemplateBuilder.requestFactory(() -> httpRequestFactory);
    //
    // return httpRequestFactory;
    // }

    /**
     * Umleiten von Port 9090 auf 8443.
     */
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected TomcatWebServer getTomcatWebServer(final Tomcat tomcat) {
                // JNDI aktivieren.
                tomcat.enableNaming();

                return super.getTomcatWebServer(tomcat);
            }

            @Override
            protected void postProcessContext(final Context context) {
                // SSL Context definieren.
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);

                // JNDI-Inhalt
                ContextEnvironment contextEnvironment = new ContextEnvironment();
                contextEnvironment.setName("test");
                contextEnvironment.setType("java.lang.String");
                contextEnvironment.setValue("MY-JNDI");
                contextEnvironment.setOverride(false);
                context.getNamingResources().addEnvironment(contextEnvironment);

                // ContextResource funktioniert nur bei "javax.sql.DataSource" und "javax.mail.Session".
                // Siehe org.apache.naming.factory.ResourceFactory.
                // ContextResource resource = new ContextResource();
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

                // ContextResourceEnvRef envRef = new ContextResourceEnvRef();
                // envRef.setName("test");
                // envRef.setType(DataSourceProperties.class.getName());
                // context.getNamingResources().addResourceEnvRef(envRef);

                // ContextResourceLink resourceLink = new ContextResourceLink();
                // resourceLink.setName("jdbc/aJNDI");
                // resourceLink.setGlobal("jdbc/aJNDIGlobal");
                // resourceLink.setType("javax.sql.DataSource");
                // resourceLink.setProperty("auth", "Container");
                //
                // context.getNamingResources().addResourceLink(resourceLink);

                //                this.logger.info(context.getNamingResources());
                LoggerFactory.getLogger(getClass()).info("{}", context.getNamingResources());
            }
        };

        tomcat.addAdditionalTomcatConnectors(servletRedirectConnector());

        return tomcat;
    }

    /**
     * Umleiten von Port 9090 auf 8443.
     */
    private Connector servletRedirectConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(9090);
        connector.setSecure(false);
        connector.setRedirectPort(this.serverPort);

        return connector;
    }
}

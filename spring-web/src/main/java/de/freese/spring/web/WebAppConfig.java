// Created: 25.10.22
package de.freese.spring.web;

import java.util.Set;

import jakarta.faces.webapp.FacesServlet;

import com.sun.faces.config.ConfigureListener;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Thomas Freese
 */
@Configuration
public class WebAppConfig {
    @Bean
    public ServletRegistrationBean<FacesServlet> facesServletRegistration() {
        // final ServletRegistrationBean<FacesServlet> servletRegistrationBean = new ServletRegistrationBean<>(new FacesServlet(), "/faces");
        final ServletRegistrationBean<FacesServlet> servletRegistrationBean = new ServletRegistrationBean<>();
        servletRegistrationBean.setServlet(new FacesServlet());
        servletRegistrationBean.setUrlMappings(Set.of("*.html", "*.xhtml", "*.jsf"));
        servletRegistrationBean.setLoadOnStartup(1);
        servletRegistrationBean.setName("Faces Servlet");

        return servletRegistrationBean;
    }

    /**
     * Not necessary with joinfaces.
     */
    @Bean
    public ServletListenerRegistrationBean<ConfigureListener> jsfConfigureListener() {
        return new ServletListenerRegistrationBean<>(new ConfigureListener());
    }

    @Bean
    public ServletWebServerFactory servletContainer() {
        return new TomcatServletWebServerFactory() {
            @Override
            protected TomcatWebServer getTomcatWebServer(final Tomcat tomcat) {
                // Enable JNDI.
                tomcat.enableNaming();

                return super.getTomcatWebServer(tomcat);
            }

            @Override
            protected void postProcessContext(final Context context) {
                // // SSL Context definieren.
                // final SecurityConstraint securityConstraint = new SecurityConstraint();
                // securityConstraint.setUserConstraint("CONFIDENTIAL");
                //
                // final SecurityCollection collection = new SecurityCollection();
                // collection.addPattern("/*");
                // securityConstraint.addCollection(collection);
                // context.addConstraint(securityConstraint);

                // JNDI-Inhalt
                final ContextEnvironment contextEnvironment = new ContextEnvironment();
                contextEnvironment.setName("test");
                contextEnvironment.setType("java.lang.String");
                contextEnvironment.setValue("MY-JNDI");
                contextEnvironment.setOverride(false);
                context.getNamingResources().addEnvironment(contextEnvironment);
            }
        };
    }

    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return servletContext -> {
            servletContext.setInitParameter("com.sun.faces.forceLoadConfiguration", Boolean.TRUE.toString());

            // servletContext.setInitParameter("jakarta.faces.CONFIG_FILES", "/WEB-INF/faces-config.xml");
            servletContext.setInitParameter("jakarta.faces.FACELETS_SKIP_COMMENTS", Boolean.TRUE.toString());
            servletContext.setInitParameter("jakarta.faces.PROJECT_STAGE", "Development");

            servletContext.setInitParameter("primefaces.CLIENT_SIDE_VALIDATION", Boolean.TRUE.toString());
            servletContext.setInitParameter("primefaces.THEME", "arya");
        };
    }

    // @Bean
    // public ServletRegistrationBean<DefaultServlet> httpServletRegistration() {
    //     final ServletRegistrationBean<DefaultServlet> servletRegistrationBean = new ServletRegistrationBean<>(new DefaultServlet(), "/");
    //     // servletRegistrationBean.setServlet(new DefaultServlet());
    //     servletRegistrationBean.setUrlMappings(Set.of("*.html"));
    //     servletRegistrationBean.setLoadOnStartup(1);
    //     servletRegistrationBean.setName("HTTP Servlet");
    //
    //     return servletRegistrationBean;
    // }
}

// Created: 25.10.22
package de.freese.spring.web;

import java.security.SecureRandom;
import java.util.Random;
import java.util.Set;

import jakarta.faces.webapp.FacesServlet;
import jakarta.servlet.ServletContainerInitializer;

import com.sun.faces.config.FacesInitializer;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import org.apache.tomcat.util.descriptor.web.ContextResourceEnvRef;
import org.jboss.weld.environment.servlet.EnhancedListener;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Thomas Freese
 */
@Configuration
public class WebAppConfig implements WebMvcConfigurer {

    // public class JsfInitializer implements ServletContextInitializer {
    //     @Override
    //     public void onStartup(ServletContext context) throws ServletException {
    //         EnhancedListener cdiInitializer = new EnhancedListener();
    //         cdiInitializer.onStartup(null, context);
    //
    //         ServletContainerInitializer facesInitializer = new FacesInitializer();
    //         facesInitializer.onStartup(null, context);
    //     }
    // }
    // @Bean
    // ServletContextInitializer facesInitializer() {
    //     return new JsfInitializer();
    // }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        // if (!registry.hasMappingForPattern("/**")) {
        //     registry.addResourceHandler("/**").addResourceLocations("classpath:/xhtml/");
        // }
    }

    // @Bean
    // ServletRegistrationBean<DefaultServlet> httpServletRegistration() {
    //     final ServletRegistrationBean<DefaultServlet> servletRegistrationBean = new ServletRegistrationBean<>(new DefaultServlet(), "/");
    //     // servletRegistrationBean.setServlet(new DefaultServlet());
    //     servletRegistrationBean.setUrlMappings(Set.of("*.html"));
    //     servletRegistrationBean.setLoadOnStartup(1);
    //     servletRegistrationBean.setName("HTTP Servlet");
    //
    //     return servletRegistrationBean;
    // }

    @Bean
    ServletRegistrationBean<FacesServlet> facesServletRegistration() {
        // final ServletRegistrationBean<FacesServlet> servletRegistrationBean = new ServletRegistrationBean<>(new FacesServlet(), "/faces");
        final ServletRegistrationBean<FacesServlet> servletRegistrationBean = new ServletRegistrationBean<>();
        servletRegistrationBean.setServlet(new FacesServlet());
        servletRegistrationBean.setUrlMappings(Set.of("*.xhtml", "*.jsf"));
        servletRegistrationBean.setLoadOnStartup(1);
        servletRegistrationBean.setName("Faces Servlet");

        return servletRegistrationBean;
    }

    @Bean
    Random random() {
        return new SecureRandom();
    }

    /**
     * Not necessary with joinfaces.
     */
    // @Bean
    // ServletListenerRegistrationBean<ConfigureListener> jsfConfigureListener() {
    //     return new ServletListenerRegistrationBean<>(new ConfigureListener());
    // }
    @Bean
    ServletWebServerFactory servletContainer() {
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

                final ContextResourceEnvRef envRef = new ContextResourceEnvRef();
                envRef.setName("BeanManager");
                envRef.setType("jakarta.enterprise.inject.spi.BeanManager");
                context.getNamingResources().addResourceEnvRef(envRef);
            }
        };
    }

    @Bean
    ServletContextInitializer servletContextInitializer() {
        return servletContext -> {
            servletContext.setInitParameter("com.sun.faces.forceLoadConfiguration", Boolean.TRUE.toString());

            // servletContext.setInitParameter("jakarta.faces.CONFIG_FILES", "/WEB-INF/faces-config.xml");
            servletContext.setInitParameter("jakarta.faces.FACELETS_SKIP_COMMENTS", Boolean.TRUE.toString());
            servletContext.setInitParameter("jakarta.faces.PROJECT_STAGE", "Development");

            servletContext.setInitParameter("primefaces.CLIENT_SIDE_VALIDATION", Boolean.TRUE.toString());
            servletContext.setInitParameter("primefaces.THEME", "arya");

            servletContext.addListener("org.jboss.weld.environment.servlet.Listener"); // CDI first
            servletContext.addListener("com.sun.faces.config.ConfigureListener");

            // servletContext.addListener("org.springframework.web.context.request.RequestContextListener");

            // Creates a new Spring-Context.
            // servletContext.addListener("org.springframework.web.context.ContextLoaderListener");

            // Only with embedded Server, not necessary with joinfaces.
            final EnhancedListener cdiInitializer = new EnhancedListener();
            cdiInitializer.onStartup(null, servletContext);

            final ServletContainerInitializer facesInitializer = new FacesInitializer();
            facesInitializer.onStartup(null, servletContext);
        };
    }
}

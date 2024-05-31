// Created: 25.10.22
package de.freese.spring.web;

import java.util.Set;

import jakarta.faces.webapp.FacesServlet;
import jakarta.servlet.ServletContext;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Thomas Freese
 */
@Configuration
public class WebAppConfig {
    @Bean
    public ServletRegistrationBean<FacesServlet> facesServletRegistration(final ServletContext servletContext) {
        // servletContext.setInitParameter("jakarta.faces.CONFIG_FILES", "/WEB-INF/faces-config.xml");
        servletContext.setInitParameter("jakarta.faces.FACELETS_SKIP_COMMENTS", Boolean.TRUE.toString());
        servletContext.setInitParameter("jakarta.faces.PROJECT_STAGE", "Development");

        // servletContext.setInitParameter("primefaces.THEME", "arya");

        // final ServletRegistrationBean<FacesServlet> servletRegistrationBean = new ServletRegistrationBean<>(new FacesServlet(), "/faces");
        final ServletRegistrationBean<FacesServlet> servletRegistrationBean = new ServletRegistrationBean<>();
        servletRegistrationBean.setServlet(new FacesServlet());
        servletRegistrationBean.setUrlMappings(Set.of("*.html", "*.xhtml", "*.jsf"));
        // servletRegistrationBean.setUrlMappings(Set.of("*.xhtml", "*.jsf"));
        servletRegistrationBean.setLoadOnStartup(1);
        servletRegistrationBean.setName("Faces Servlet");

        return servletRegistrationBean;
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

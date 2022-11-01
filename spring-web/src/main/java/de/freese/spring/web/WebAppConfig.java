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
public class WebAppConfig
{
    //    @Bean
    //    public WebMvcConfigurer configureResourceHandlers(final ResourceHandlerRegistry registry)
    //    {
    //        return
    //                registry.addResourceHandler("/");
    //    }

    @Bean
    public ServletRegistrationBean<FacesServlet> facesServletRegistration(ServletContext servletContext)
    {
        //        servletContext.setInitParameter("jakarta.faces.CONFIG_FILES", "/WEB-INF/faces-config.xml");
        servletContext.setInitParameter("jakarta.faces.FACELETS_SKIP_COMMENTS", Boolean.TRUE.toString());
        //        servletContext.setInitParameter("jakarta.faces.PROJECT_STAGE", "Development");

        // Damit JSF ohne web.xml funktioniert.
        servletContext.setInitParameter("com.sun.faces.forceLoadConfiguration", Boolean.TRUE.toString());
        //        servletContext.setInitParameter("com.sun.faces.expressionFactory", "org.apache.el.ExpressionFactoryImpl");
        //        servletContext.setInitParameter("jakarta.faces.context.FacesContextFactory", "org.primefaces.context.PrimeFacesContextFactory");
        //        servletContext.setInitParameter("com.sun.faces.cdi.BeanManager", "org.primefaces.context.PrimeFacesContextFactory");

        //        servletContext.setInitParameter("primefaces.THEME", "arya");

        ServletRegistrationBean<FacesServlet> servletRegistrationBean = new ServletRegistrationBean<>();
        servletRegistrationBean.setServlet(new FacesServlet());
        servletRegistrationBean.setUrlMappings(Set.of("*.xhtml", "*.jsf"));
        servletRegistrationBean.setLoadOnStartup(1);
        servletRegistrationBean.setName("Faces Servlet");

        return servletRegistrationBean;
    }

    //    /**
    //     * Keine Factory als Backup für jakarta.faces.context.FacesContextFactory gefunden.
    //     */
    //    @Bean
    //    public ServletListenerRegistrationBean<ConfigureListener> jsfConfigureListener()
    //    {
    //        return new ServletListenerRegistrationBean<>(new ConfigureListener());
    //    }
}

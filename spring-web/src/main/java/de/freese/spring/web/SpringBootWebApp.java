// Created: 10.06.2015
package de.freese.spring.web;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;

import javax.faces.webapp.FacesServlet;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Starten über: mvn spring-boot:run
 *
 * @author Thomas Freese
 */
@SpringBootApplication
public class SpringBootWebApp //implements WebMvcConfigurer
{
    public static void main(final String[] args) throws Exception
    {
        ApplicationContext context = SpringApplication.run(SpringBootWebApp.class, args);

        int port = context.getEnvironment().getProperty("local.server.port", Integer.class);
        Optional<String> contextPath = Optional.ofNullable(context.getEnvironment().getProperty("server.servlet.context-path", String.class));

        URL url = new URL("http://localhost:" + port + contextPath.orElse("") + "/index.xhtml");
        URI uri = url.toURI();

        try
        {
            // Firefox: view-source:URI
            Runtime.getRuntime().exec(new String[]
                    {
                            "C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe", "-new-tab", url.toString()
                    });
        }
        catch (Exception ex)
        {
            try
            {
                // Linux
                Runtime.getRuntime().exec(new String[]
                        {
                                "firefox", "-new-tab", url.toString()
                        });
            }
            catch (Exception ex2)
            {
                // Default
                Desktop.getDesktop().browse(uri);
            }
        }
    }

    //    @Override
    //    public void addResourceHandlers(final ResourceHandlerRegistry registry)
    //    {
    //        registry.addResourceHandler("/");
    //    }

    @Bean
    ServletRegistrationBean<Servlet> jsfServletRegistration(ServletContext servletContext)
    {
        servletContext.setInitParameter("jakarta.faces.CONFIG_FILES", "/WEB-INF/faces-config.xml");
        servletContext.setInitParameter("jakarta.faces.FACELETS_SKIP_COMMENTS", Boolean.TRUE.toString());
        servletContext.setInitParameter("jakarta.faces.PROJECT_STAGE", "Development");
        servletContext.setInitParameter("jakarta.faces.STATE_SAVING_METHOD", "server");
        servletContext.setInitParameter("jakarta.faces.VALIDATE_EMPTY_FIELDS", Boolean.TRUE.toString());

        servletContext.setInitParameter("com.sun.faces.compressViewState", Boolean.TRUE.toString());
        servletContext.setInitParameter("com.sun.faces.enableMissingResourceLibraryDetection", Boolean.TRUE.toString());

        // Damit JSF ohne web.xml funktioniert.
        servletContext.setInitParameter("com.sun.faces.forceLoadConfiguration", Boolean.TRUE.toString());

        // Verhindert Fehlermeldungen wie "JSP-Version des Containers ist älter als ..."
        servletContext.setInitParameter("com.sun.faces.expressionFactory", "org.apache.el.ExpressionFactoryImpl");

        servletContext.setInitParameter("org.primefaces.extensions.DELIVER_UNCOMPRESSED_RESOURCES", Boolean.FALSE.toString());

        servletContext.setInitParameter("primefaces.CLIENT_SIDE_VALIDATION", Boolean.TRUE.toString());

        // servletContext.setInitParameter("primefaces.THEME", "aristo");
        servletContext.setInitParameter("primefaces.THEME", "afterdark");

        // Registration
        ServletRegistrationBean<Servlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new FacesServlet());
        srb.setUrlMappings(Arrays.asList("*.xhtml"));
        srb.setLoadOnStartup(1);

        return srb;
    }
}

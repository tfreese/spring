// Erzeugt: 10.06.2015
package de.freese.web;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import javax.faces.webapp.FacesServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import com.sun.faces.config.ConfigureListener;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
// @Configuration
// @EnableAutoConfiguration(exclude = // Spring MVC ausschalten, dann gehen die Endpoints aber nicht mehr.
// {
// WebMvcAutoConfiguration.class, DispatcherServletAutoConfiguration.class
// })
// @ComponentScan("de.freese.web")
// @PropertySource("classpath:application.properties") // Default, wird automatisch geladen
public class SpringBootWebApp extends SpringBootServletInitializer implements ServletContextInitializer // ServletContextAware
{
    /**
     * Konfiguriert die SpringApplication.
     *
     * @param builder {@link SpringApplicationBuilder}
     * @return {@link SpringApplicationBuilder}
     */
    private static SpringApplicationBuilder configureApplication(final SpringApplicationBuilder builder)
    {
        //@formatter:off
        return builder
            .sources(SpringBootWebApp.class)
            .bannerMode(Banner.Mode.OFF)
            .headless(true)
            .registerShutdownHook(true);
        //@formatter:on
        // .listeners(new ApplicationPidFileWriter("spring-boot-web.pid"))
        // .web(false)
    }

    /**
     * @param args String[]
     * @throws Exception Falls was schief geht.
     */
    public static void main(final String[] args) throws Exception
    {
        // ConfigurableApplicationContext ctx = SpringApplication.run(Main.class, args);
        // ctx.registerShutdownHook();

        // SpringApplication application = new SpringApplication(Main.class);
        // application.setBannerMode(Banner.Mode.OFF);
        // application.setRegisterShutdownHook(true);
        //
        // try (ConfigurableApplicationContext ctx = application.run(args))
        // {
        // ctx.registerShutdownHook();
        // }
        //
        // SpringApplication.run(SpringBootWebApp.class, args);
        ApplicationContext context = configureApplication(new SpringApplicationBuilder()).run(args);

        int port = context.getEnvironment().getProperty("local.server.port", Integer.class);
        Optional<String> contextPath = Optional.ofNullable(context.getEnvironment().getProperty("server.servlet.context-path", String.class));

        URL url = new URL("http://localhost:" + port + contextPath.orElse(""));
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
                // IE
                Desktop.getDesktop().browse(uri);
            }
        }

        // -Drun_in_ide=true
        // In der Runtime als Default VM-Argument setzen oder in der eclipse.ini
        if (Boolean.parseBoolean(System.getenv("run_in_ide")) || Boolean.parseBoolean(System.getProperty("run_in_ide", "false")))
        {
            System.out.println();
            System.out.println("******************************************************************************************************************");
            System.out.println("You're using an IDE, click in this console and press ENTER to call System.exit() and trigger the shutdown routine.");
            System.out.println("******************************************************************************************************************");

            try
            {
                System.in.read();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            System.exit(0);
        }
    }

    /**
     * POM:<br>
     * &lt;packaging>&gt;war&lt;/packaging&gt;<<br>
     * Tomcat aus spring-boot-starter-web excludieren und explizit auf provided setzen.<br>
     * Alle anderen J2EE-Jars auf provided setzen.
     *
     * @see org.springframework.boot.web.servlet.support.SpringBootServletInitializer#configure(org.springframework.boot.builder.SpringApplicationBuilder)
     */
    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder application)
    {
        return configureApplication(application);
    }

    /**
     * @return {@link ServletRegistrationBean}
     */
    @Bean
    public ServletRegistrationBean<FacesServlet> facesServletRegistration()
    {
        ServletRegistrationBean<FacesServlet> servletRegistrationBean = new ServletRegistrationBean<>(new FacesServlet(), "*.xhtml", "*.jsf");
        servletRegistrationBean.setName("Faces Servlet");
        servletRegistrationBean.setLoadOnStartup(1);

        return servletRegistrationBean;
    }

    /**
     * web.xml Listener
     *
     * @return {@link ServletListenerRegistrationBean}
     */
    @Bean
    public ServletListenerRegistrationBean<ConfigureListener> jsfConfigureListener()
    {
        return new ServletListenerRegistrationBean<>(new ConfigureListener());
    }

    /**
     * @see org.springframework.boot.web.servlet.ServletContextInitializer#onStartup(javax.servlet.ServletContext)
     */
    @Override
    public void onStartup(final ServletContext sc) throws ServletException
    {
        sc.setInitParameter("javax.faces.CONFIG_FILES", "/WEB-INF/faces-config.xml");
        sc.setInitParameter("javax.faces.FACELETS_SKIP_COMMENTS", "true");
        sc.setInitParameter("javax.faces.PROJECT_STAGE", "Development");
        sc.setInitParameter("javax.faces.STATE_SAVING_METHOD", "server");
        sc.setInitParameter("javax.faces.VALIDATE_EMPTY_FIELDS", "true");

        sc.setInitParameter("com.sun.faces.compressViewState", "true");
        sc.setInitParameter("com.sun.faces.enableMissingResourceLibraryDetection", "true");

        // Damit JSF ohne web.xml funktioniert.
        sc.setInitParameter("com.sun.faces.forceLoadConfiguration", "TRUE");

        // Verhindert Fehlermeldungen wie "JSP-Version des Containers ist älter als ..."
        sc.setInitParameter("com.sun.faces.expressionFactory", "org.apache.el.ExpressionFactoryImpl");

        sc.setInitParameter("org.primefaces.extensions.DELIVER_UNCOMPRESSED_RESOURCES", "false");

        sc.setInitParameter("primefaces.CLIENT_SIDE_VALIDATION", "true");
        // sc.setInitParameter("primefaces.THEME", "aristo");
        sc.setInitParameter("primefaces.THEME", "afterdark");
    }

    /**
     * web.xml Listener<br>
     * Verhindert Meldungen wie FacesRequestAttributes#registerDestructionCallback - Could not register destruction callback ...
     *
     * @return {@link ServletListenerRegistrationBean}
     */
    // @Bean
    // public ServletListenerRegistrationBean<RequestContextListener> requestContextListener()
    // {
    // return new ServletListenerRegistrationBean<>(new RequestContextListener());
    // }
    // /**
    // *
    // * @return EmbeddedServletContainerFactory
    // */
    // @Bean
    // public EmbeddedServletContainerFactory embeddedServletContainerFactory()
    // {
    // JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory();
    // factory.addServerCustomizers(server ->
    // {
    //// QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
    // ThreadPool threadPool = server.getThreadPool();
    //
    // if (threadPool instanceof QueuedThreadPool)
    // {
    // ((QueuedThreadPool) threadPool).setMinThreads(4);
    // ((QueuedThreadPool) threadPool).setMaxThreads(4);
    // }
    // else if (threadPool instanceof ExecutorThreadPool)
    // {
    // }
    // });
    //
    // return factory;
    // }
    // /**
    // * Allows the use of @Scope("view") on Spring @Component, @Service and @Controller
    // * beans
    // */
    // @Bean
    // public static CustomScopeConfigurer scopeConfigurer()
    // {
    // CustomScopeConfigurer configurer = new CustomScopeConfigurer();
    // Map<String, Object> hashMap = new HashMap<>();
    // hashMap.put("view", new ViewScope());
    // configurer.setScopes(hashMap);
    //
    // return configurer;
    // }
    //
    // @Bean
    // public ViewResolver getViewResolver()
    // {
    // InternalResourceViewResolver resolver = new InternalResourceViewResolver();
    // resolver.setPrefix("/templates/");
    // resolver.setSuffix(".xhtml");
    // return resolver;
    // }
}

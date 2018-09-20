/**
 * Created: 02.09.2018
 */
package de.freese.spring.thymeleaf.config;

import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import de.freese.spring.thymeleaf.ThymeleafApplication;

/**
 * @author Thomas Freese
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer, AsyncConfigurer
{
    /**
     * Erzeugt eine neue Instanz von {@link MvcConfig}.
     */
    public MvcConfig()
    {
        super();
    }

    /**
     * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry)
     */
    @Override
    public void addInterceptors(final InterceptorRegistry registry)
    {
        registry.addInterceptor(localeChangeInterceptor());

        // Parsen von HTTPS-Headern vor der Verarbeitung des Requests.
        registry.addInterceptor(new HandlerInterceptor()
        {
            /**
             * @see org.springframework.web.servlet.HandlerInterceptor#preHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
             *      java.lang.Object)
             */
            @Override
            public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception
            {
                // System.out.println("HandlerInterceptor: " + Thread.currentThread().getName());

                System.out.println();
                System.out.println("Request: " + new Date());

                Enumeration<String> headerNames = request.getHeaderNames();

                while (headerNames.hasMoreElements())
                {
                    String headerName = headerNames.nextElement();
                    System.out.printf("%s = %s%n", headerName, request.getHeader(headerName));
                }

                return true;
            }
        });
    }

    /**
     * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#addViewControllers(org.springframework.web.servlet.config.annotation.ViewControllerRegistry)
     */
    @Override
    public void addViewControllers(final ViewControllerRegistry registry)
    {
        // Wird schon im PersonThymeleafController gemacht.
        // registry.addViewController("/").setViewName("index");
        // registry.addViewController("/index").setViewName("index");
    }

    /**
     * Executer für die Verarbeitung der HTTP-Requests.<br>
     * Verlagert die asynchrone Ausführung von Server-Requests (Callable, WebAsyncTask) in diesen ThreadPool.<br>
     * Ansonsten würde für jeden Request immer ein neuer Thread erzeugt, siehe TaskExecutor des RequestMappingHandlerAdapter.<br>
     *
     * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#configureAsyncSupport(org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer)
     */
    @Override
    public void configureAsyncSupport(final AsyncSupportConfigurer configurer)
    {
        configurer.setTaskExecutor(springTaskExecutor());
    }

    /**
     * JSON als Default, alternativ XML über ACCEPT-Header.
     *
     * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#configureContentNegotiation(org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer)
     */
    @Override
    public void configureContentNegotiation(final ContentNegotiationConfigurer configurer)
    {
        // @formatter:off
        configurer
            .favorPathExtension(false) // URL.xml -> Liefert XML
            .favorParameter(false).parameterName("format") // URL?format=xml -> Liefert XML
            .ignoreAcceptHeader(false)
            .useRegisteredExtensionsOnly(true)
            .ignoreUnknownPathExtensions(false)
            .defaultContentType(MediaType.APPLICATION_JSON_UTF8)
            .mediaType("xml", MediaType.APPLICATION_XML)
            .mediaType("json", MediaType.APPLICATION_JSON_UTF8);
        // @formatter:on
    }

    /**
     * @return {@link ThreadPoolExecutorFactoryBean}
     */
    @Bean
    @Primary
    public ThreadPoolExecutorFactoryBean executorService()
    {
        int coreSize = Runtime.getRuntime().availableProcessors() * 2;
        int maxSize = coreSize * 2;
        int queueSize = maxSize * 2;
        int keepAliveSeconds = 60;

        ThreadPoolExecutorFactoryBean bean = new ThreadPoolExecutorFactoryBean();
        bean.setCorePoolSize(coreSize);
        bean.setMaxPoolSize(maxSize);
        bean.setQueueCapacity(queueSize);
        bean.setKeepAliveSeconds(keepAliveSeconds);
        bean.setThreadPriority(Thread.NORM_PRIORITY);
        bean.setThreadNamePrefix("server-");
        bean.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        bean.setAllowCoreThreadTimeOut(false);
        bean.setExposeUnconfigurableExecutor(true);

        return bean;
    }

    /**
     * Processing Secure Methods Asynchronously.
     *
     * @see org.springframework.scheduling.annotation.AsyncConfigurer#getAsyncExecutor()
     */
    @Override
    public Executor getAsyncExecutor()
    {
        return new DelegatingSecurityContextExecutorService(executorService().getObject());
    }

    /**
     * @see org.springframework.scheduling.annotation.AsyncConfigurer#getAsyncUncaughtExceptionHandler()
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler()
    {
        return (ex, method, params) -> ThymeleafApplication.LOGGER.error(ex.getMessage());
    }

    /**
     * URL-Parameter ändert Sprache: URL/?lang=en<br>
     *
     * @return {@link LocaleChangeInterceptor}
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor()
    {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");

        return localeChangeInterceptor;
    }

    /**
     * LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
     *
     * @return {@link LocaleResolver}
     */
    @Bean
    public LocaleResolver localeResolver()
    {
        // CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        // localeResolver.setCookieName("mycookie");
        // localeResolver.setCookieMaxAge(60 * 60); // 60 Minuten

        // AcceptHeaderLocaleResolver acceptHeaderLocaleResolver = new AcceptHeaderLocaleResolver();
        // acceptHeaderLocaleResolver.setDefaultLocale(Locale.GERMAN);

        // Ohne #setDefaultLocale wird im "Accept-Language" Header nachgeschaut.
        SessionLocaleResolver localeResolver = new SessionLocaleResolver()
        {
            /**
             * @see org.springframework.web.servlet.i18n.SessionLocaleResolver#determineDefaultLocale(javax.servlet.http.HttpServletRequest)
             */
            @Override
            protected Locale determineDefaultLocale(final HttpServletRequest request)
            {
                Locale defaultLocale = request.getLocale();

                if (defaultLocale == null)
                {
                    defaultLocale = getDefaultLocale();
                }

                return defaultLocale;
            }
        };
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        localeResolver.setDefaultTimeZone(TimeZone.getTimeZone("Europe/Berlin"));

        return localeResolver;
    }

    /**
     * LocaleContextHolder.getLocale()<br>
     * The most awesome website is {0}<br>
     * #{message.mostAwesomeWebsite(${website})}
     *
     * @return {@link MessageSource}
     */
    @Bean
    public MessageSource messageSource()
    {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("static/i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        // messageSource.setCacheSeconds(60 * 60); // 60 Minuten, -1 = kein Refresh

        return messageSource;
    }

    /**
     * @return {@link ScheduledExecutorFactoryBean}
     */
    @Bean
    @ConditionalOnMissingBean(ScheduledExecutorService.class)
    public ScheduledExecutorFactoryBean scheduledExecutorService()
    {
        int poolSize = Runtime.getRuntime().availableProcessors();

        ScheduledExecutorFactoryBean bean = new ScheduledExecutorFactoryBean();
        bean.setPoolSize(poolSize);
        bean.setThreadPriority(Thread.NORM_PRIORITY);
        bean.setThreadNamePrefix("scheduler-");
        bean.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        bean.setExposeUnconfigurableExecutor(true);

        return bean;
    }

    /**
     * Wird für {@link EnableAsync} benötigt.
     *
     * @return {@link TaskExecutor}
     */
    @Bean(
    {
            "taskExecutor", "asyncTaskExecutor"
    })
    @ConditionalOnMissingBean(
    {
            AsyncTaskExecutor.class, TaskExecutor.class
    })
    // public TaskExecutor springTaskExecutor(@Qualifier("executorService") final ExecutorService executorService)
    public AsyncTaskExecutor springTaskExecutor()
    {
        ThymeleafApplication.LOGGER.info("no TaskExecutor exist, create a ConcurrentTaskExecutor");

        AsyncTaskExecutor bean = new ConcurrentTaskExecutor(executorService().getObject());

        return bean;
    }

    /**
     * Wird für {@link EnableScheduling} benötigt.
     *
     * @param executorService {@link ExecutorService}
     * @param scheduledExecutorService {@link ScheduledExecutorService}
     * @return {@link TaskScheduler}
     */
    @Bean("taskScheduler")
    @ConditionalOnMissingBean(TaskScheduler.class)
    public TaskScheduler springTaskScheduler(@Qualifier("executorService") final ExecutorService executorService,
                                             final ScheduledExecutorService scheduledExecutorService)
    {
        ThymeleafApplication.LOGGER.info("no TaskScheduler exist, create a ConcurrentTaskScheduler");

        TaskScheduler bean = new ConcurrentTaskScheduler(executorService, scheduledExecutorService);

        return bean;
    }
}

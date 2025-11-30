// Created: 02.09.2018
package de.freese.spring.thymeleaf.config;

import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

/**
 * @author Thomas Freese
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer, AsyncConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MvcConfig.class);

    // @Bean
    // Jackson2JsonEncoder jackson2JsonEncoder(ObjectMapper objectMapper){
    //     return new Jackson2JsonEncoder(objectMapper);
    // }
    //
    // @Bean
    // Jackson2JsonDecoder jackson2JsonDecoder(ObjectMapper objectMapper){
    //     return new Jackson2JsonDecoder(objectMapper);
    // }
    //
    // @Bean
    // WebFluxConfigurer webFluxConfigurer(Jackson2JsonEncoder encoder, Jackson2JsonDecoder decoder){
    //     return new WebFluxConfigurer() {
    //         @Override
    //         public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
    //             configurer.defaultCodecs().jackson2JsonEncoder(encoder);
    //             configurer.defaultCodecs().jackson2JsonDecoder(decoder);
    //         }
    //     };
    // }

    // Configured in application.yml
    // /**
    //  * <a href="https://www.baeldung.com/spring-boot-customize-jackson-objectmapper">spring-boot-customize-jackson-objectmapper</a>
    //  */
    // @Bean
    // @Primary
    // public ObjectMapper objectMapper() {
    //     final JavaTimeModule javaTimeModule = new JavaTimeModule();
    //     // module.addSerializer(LOCAL_DATETIME_SERIALIZER);
    //
    //     final ObjectMapper jsonMapper = new ObjectMapper()
    //             .enable(SerializationFeature.INDENT_OUTPUT)
    //             .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
    //             .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    //             .registerModule(javaTimeModule)
    //             // .setVisibility(PropertyAccessor.FIELD, Visibility.NONE)
    //             // .setVisibility(PropertyAccessor.SETTER, Visibility.PUBLIC_ONLY)
    //             // .setVisibility(PropertyAccessor.GETTER, Visibility.PUBLIC_ONLY)
    //             ;
    //
    //     jsonMapper.setLocale(Locale.GERMANY);
    //
    //     final TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
    //     jsonMapper.setTimeZone(timeZone);
    //
    //     // SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //     // df.setTimeZone(timeZone);
    //     // jsonMapper.setDateFormat(df);
    //     return jsonMapper;
    // }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());

        // Parse HTTPS-Header before the Request is processed.
        // registry.addInterceptor(new HandlerInterceptor() {
        //     @Override
        //     public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        //         // System.out.println("HandlerInterceptor: " + Thread.currentThread().getName());
        //
        //         LOGGER.info("Request: {}", LocalDateTime.now());
        //
        //         final Enumeration<String> headerNames = request.getHeaderNames();
        //
        //         while (headerNames.hasMoreElements()) {
        //             final String headerName = headerNames.nextElement();
        //             LOGGER.info("{} = {}", headerName, request.getHeader(headerName));
        //         }
        //
        //         return true;
        //     }
        // });
    }

    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        // Wird schon im HomeThymeleafController gemacht.
        // registry.addViewController("/").setViewName("index");
        // registry.addViewController("/index").setViewName("index");
    }

    /**
     * Executer for processing HTTP-Requests.<br>
     * Delegates the Server-Requests (Callable, WebAsyncTask) in these ThreadPool.<br>
     * Otherwise a new Thread is created for each Request, see TaskExecutor for RequestMappingHandlerAdapter.<br>
     */
    @Override
    public void configureAsyncSupport(final AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(springTaskExecutor());
    }

    /**
     * JSON as Default, alternativ XML over ACCEPT-Header.
     */
    @Override
    public void configureContentNegotiation(final ContentNegotiationConfigurer configurer) {
        configurer
                //.favorPathExtension(false) // URL.xml -> Delivers XML; Replace with strategies(strategies)
                .favorParameter(false).parameterName("format") // URL?format=xml -> Delivers XML
                .ignoreAcceptHeader(false)
                .useRegisteredExtensionsOnly(true)
                //.ignoreUnknownPathExtensions(false) // Replace with strategies(strategies)
                .defaultContentType(MediaType.APPLICATION_JSON)
                .mediaType("xml", MediaType.APPLICATION_XML)
                .mediaType("json", MediaType.APPLICATION_JSON);
    }

    @Bean
    @ConditionalOnMissingBean({Executor.class, ExecutorService.class})
    @Primary
    public ThreadPoolExecutorFactoryBean executorService() {
        final int coreSize = Math.max(2, Runtime.getRuntime().availableProcessors() / 4);
        final int maxSize = coreSize * 2;
        final int queueSize = maxSize * 4;
        final int keepAliveSeconds = 60;

        final ThreadPoolExecutorFactoryBean bean = new ThreadPoolExecutorFactoryBean();
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

    @Override
    public Executor getAsyncExecutor() {
        final ExecutorService executorService = Objects.requireNonNull(executorService().getObject(), "executorService required");

        return new DelegatingSecurityContextExecutorService(executorService);
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> LOGGER.error(ex.getMessage());
    }

    /**
     * URL-Parameter Changes Sprache: URL/?lang=en<br>
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        final LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");

        return localeChangeInterceptor;
    }

    /**
     * LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
     */
    @Bean
    public LocaleResolver localeResolver() {
        // final CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        // localeResolver.setCookieName("mycookie");
        // localeResolver.setCookieMaxAge(60 * 60); // 60 Minuten

        // final AcceptHeaderLocaleResolver acceptHeaderLocaleResolver = new AcceptHeaderLocaleResolver();
        // acceptHeaderLocaleResolver.setDefaultLocale(Locale.GERMAN);

        // Without #setDefaultLocale "Accept-Language" Header is used.
        final SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        // localeResolver.setDefaultLocale(null);
        localeResolver.setDefaultTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        localeResolver.setDefaultLocaleFunction(request -> {
            final Locale defaultLocale = request.getLocale();

            if (defaultLocale == null) {
                return Locale.ENGLISH;
            }

            return defaultLocale;
        });

        return localeResolver;
    }

    /**
     * LocaleContextHolder.getLocale()<br>
     * The most awesome website is {0}<br>
     * #{message.mostAwesomeWebsite(${website})}
     */
    @Bean
    public MessageSource messageSource() {
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("static/i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        // messageSource.setCacheSeconds(60 * 60); // 60 Minuten, -1 = no Refresh

        return messageSource;
    }

    @Bean
    @ConditionalOnMissingBean(ScheduledExecutorService.class)
    public ScheduledExecutorFactoryBean scheduledExecutorService() {
        final int poolSize = Math.max(2, Runtime.getRuntime().availableProcessors() / 4);

        final ScheduledExecutorFactoryBean bean = new ScheduledExecutorFactoryBean();
        bean.setPoolSize(poolSize);
        bean.setThreadPriority(Thread.NORM_PRIORITY);
        bean.setThreadNamePrefix("scheduler-");
        bean.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        bean.setExposeUnconfigurableExecutor(true);

        return bean;
    }

    /**
     * Wird für {@link EnableAsync} benötigt.
     */
    @Bean({"taskExecutor", "asyncTaskExecutor"})
    @ConditionalOnMissingBean({AsyncTaskExecutor.class, TaskExecutor.class})
    // public AsyncTaskExecutor springTaskExecutor(@Qualifier("executorService") final ExecutorService executorService)
    public AsyncTaskExecutor springTaskExecutor() {
        LOGGER.info("no TaskExecutor exist, create a ConcurrentTaskExecutor");

        return new ConcurrentTaskExecutor(executorService().getObject());
        // return new ConcurrentTaskExecutor(executorService);
    }

    /**
     * Wird für {@link EnableScheduling} benötigt.
     */
    @Bean("taskScheduler")
    @ConditionalOnMissingBean(TaskScheduler.class)
    public TaskScheduler springTaskScheduler(@Qualifier("executorService") final ExecutorService executorService, final ScheduledExecutorService scheduledExecutorService) {
        LOGGER.info("no TaskScheduler exist, create a ConcurrentTaskScheduler");

        return new ConcurrentTaskScheduler(executorService, scheduledExecutorService);
    }
}

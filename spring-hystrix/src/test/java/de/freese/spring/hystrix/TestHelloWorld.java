// Created: 17.12.2016
package de.freese.spring.hystrix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.Future;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import rx.Observable;
import rx.Observer;

/**
 * <a href="https://github.com/Netflix/Hystrix/wiki/How-To-Use">Hystrix</a><br>
 * <a href="https://github.com/Netflix/Hystrix/wiki/Configuration">Configuration</a><br>
 * <a href="https://github.com/Netflix/RxJava/wiki/How-To-Use">RxJava</a><br>
 *
 * @author Thomas Freese
 */
class TestHelloWorld {
    private static HystrixRequestContext context;

    @AfterAll
    static void afterAll() {
        // Empty
    }

    @BeforeAll
    static void beforeAll() {
        // // configuration from a dynamic source
        // final PolledConfigurationSource source = createMyOwnSource();
        // AbstractPollingScheduler scheduler = createMyOwnScheduler();
        // DynamicConfiguration dynamicConfiguration =
        // new DynamicConfiguration(source, scheduler);
        //
        // // configuration from system properties
        // final ConcurrentMapConfiguration configFromSystemProperties =
        // new ConcurrentMapConfiguration(new SystemConfiguration());
        //
        // // configuration from local properties file
        // final String fileName = "...";
        // final ConcurrentMapConfiguration configFromPropertiesFile =
        // new ConcurrentMapConfiguration(new PropertiesConfiguration(fileName));
        //
        // // create a hierarchy of configuration that makes
        // // 1) dynamic configuration source override system properties and,
        // // 2) system properties override properties file
        // final ConcurrentCompositeConfiguration finalConfig = new ConcurrentCompositeConfiguration();
        // finalConfig.add(dynamicConfiguration, "dynamicConfig");
        // finalConfig.add(configFromSystemProperties, "systemConfig");
        // finalConfig.add(configFromPropertiesFile, "fileConfig");
        //
        // // install with ConfigurationManager so that finalConfig becomes the source of dynamic properties
        // final ConfigurationManager.install(finalConfig);

        // Was eigentlich bei Hystrix NICHT sein soll, nur einen ThreadPool für alles !
        // final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        // final HystrixPlugins.getInstance().registerConcurrencyStrategy(new MyHystrixConcurrencyStrategy(executor));

        // Context für Request-Caching, bei Web-Anwendungen pro Request aufrufen.
        context = HystrixRequestContext.initializeContext();
    }

    protected static HystrixRequestContext getContext() {
        return context;
    }

    @Test
    void testAsynchronous() throws Exception {
        final Future<String> fWorld = new CommandHelloWorld("World").queue();
        final Future<String> fBob = new CommandHelloWorld("Bob").queue();

        assertEquals("Hello World!", fWorld.get());
        assertEquals("Hello Bob!", fBob.get());
    }

    /**
     * (expected = RuntimeException.class)
     */
    @Test
    void testFailAsynchronous() throws Exception {
        final Future<String> fWorld = new CommandHelloFailure("World").queue();
        final Future<String> fBob = new CommandHelloFailure("Bob").queue();

        assertEquals("Hello Failure World!", fWorld.get());
        assertEquals("Hello Failure Bob!", fBob.get());
    }

    /**
     * (expected = RuntimeException.class)
     */
    @Test
    void testFailSynchronous() {
        assertEquals("Hello Failure World!", new CommandHelloFailure("World").execute());
        assertEquals("Hello Failure Bob!", new CommandHelloFailure("Bob").execute());
    }

    @Test
    void testObservable() throws Exception {
        final Observable<String> oWorld = new CommandHelloWorld("World").observe();
        final Observable<String> oBob = new CommandHelloWorld("Bob").observe();

        // blocking
        assertEquals("Hello World!", oWorld.toBlocking().single());
        assertEquals("Hello Bob!", oBob.toBlocking().single());

        // non-blocking
        // - this is a verbose anonymous inner-class approach and doesn't do assertions
        oWorld.subscribe(new Observer<>() {
            @Override
            public void onCompleted() {
                // nothing needed here
            }

            @Override
            public void onError(final Throwable e) {
                System.out.println("onError: " + e.getLocalizedMessage());
            }

            @Override
            public void onNext(final String v) {
                System.out.println("onNext: " + v);
            }
        });

        // non-blocking
        oBob.subscribe(v -> System.out.println("onNext: " + v));
    }

    @Test
    void testSynchronous() {
        // execute() = queue().get()
        assertEquals("Hello World!", new CommandHelloWorld("World").execute());
        assertEquals("Hello Bob!", new CommandHelloWorld("Bob").execute());
    }
}

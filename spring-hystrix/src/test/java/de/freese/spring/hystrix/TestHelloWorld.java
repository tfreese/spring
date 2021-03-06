/**
 * Created: 17.12.2016
 */
package de.freese.spring.hystrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import rx.Observable;
import rx.Observer;

/**
 * https://github.com/Netflix/Hystrix/wiki/How-To-Use<br>
 * https://github.com/Netflix/Hystrix/wiki/Configuration<br>
 * https://github.com/Netflix/RxJava/wiki/How-To-Use<br>
 *
 * @author Thomas Freese
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class TestHelloWorld
{
    /**
     *
     */
    private static HystrixRequestContext context = null;

    /**
     *
     */
    @AfterAll
    static void afterAll()
    {
        // Empty
    }

    /**
     *
     */
    @BeforeAll
    static void beforeAll()
    {
        // // configuration from a dynamic source
        // PolledConfigurationSource source = createMyOwnSource();
        // AbstractPollingScheduler scheduler = createMyOwnScheduler();
        // DynamicConfiguration dynamicConfiguration =
        // new DynamicConfiguration(source, scheduler);
        //
        // // configuration from system properties
        // ConcurrentMapConfiguration configFromSystemProperties =
        // new ConcurrentMapConfiguration(new SystemConfiguration());
        //
        // // configuration from local properties file
        // String fileName = "...";
        // ConcurrentMapConfiguration configFromPropertiesFile =
        // new ConcurrentMapConfiguration(new PropertiesConfiguration(fileName));
        //
        // // create a hierarchy of configuration that makes
        // // 1) dynamic configuration source override system properties and,
        // // 2) system properties override properties file
        // ConcurrentCompositeConfiguration finalConfig = new ConcurrentCompositeConfiguration();
        // finalConfig.add(dynamicConfiguration, "dynamicConfig");
        // finalConfig.add(configFromSystemProperties, "systemConfig");
        // finalConfig.add(configFromPropertiesFile, "fileConfig");
        //
        // // install with ConfigurationManager so that finalConfig becomes the source of dynamic properties
        // ConfigurationManager.install(finalConfig);

        // Was eigentlich bei Hystrix NICHT sein soll, nur einen ThreadPool für alles !
        // ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        // HystrixPlugins.getInstance().registerConcurrencyStrategy(new MyHystrixConcurrencyStrategy(executor));

        // Context für Request-Caching, bei Web-Anwendungen pro Request aufrufen.
        context = HystrixRequestContext.initializeContext();
    }

    /**
     * @return {@link HystrixRequestContext}
     */
    protected static HystrixRequestContext getContext()
    {
        return context;
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testAsynchronous() throws Exception
    {
        Future<String> fWorld = new CommandHelloWorld("World").queue();
        Future<String> fBob = new CommandHelloWorld("Bob").queue();

        assertEquals("Hello World!", fWorld.get());
        assertEquals("Hello Bob!", fBob.get());
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test// (expected = RuntimeException.class)
    void testFailAsynchronous() throws Exception
    {
        Future<String> fWorld = new CommandHelloFailure("World").queue();
        Future<String> fBob = new CommandHelloFailure("Bob").queue();

        assertEquals("Hello Failure World!", fWorld.get());
        assertEquals("Hello Failure Bob!", fBob.get());
    }

    /**
     *
     */
    @Test// (expected = RuntimeException.class)
    void testFailSynchronous()
    {
        assertEquals("Hello Failure World!", new CommandHelloFailure("World").execute());
        assertEquals("Hello Failure Bob!", new CommandHelloFailure("Bob").execute());
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testObservable() throws Exception
    {
        Observable<String> oWorld = new CommandHelloWorld("World").observe();
        Observable<String> oBob = new CommandHelloWorld("Bob").observe();

        // blocking
        assertEquals("Hello World!", oWorld.toBlocking().single());
        assertEquals("Hello Bob!", oBob.toBlocking().single());

        // non-blocking
        // - this is a verbose anonymous inner-class approach and doesn't do assertions
        oWorld.subscribe(new Observer<String>()
        {
            /**
             * @see rx.Observer#onCompleted()
             */
            @Override
            public void onCompleted()
            {
                // nothing needed here
            }

            /**
             * @see rx.Observer#onError(java.lang.Throwable)
             */
            @Override
            public void onError(final Throwable e)
            {
                System.out.println("onError: " + e.getLocalizedMessage());
            }

            /**
             * @see rx.Observer#onNext(java.lang.Object)
             */
            @Override
            public void onNext(final String v)
            {
                System.out.println("onNext: " + v);
            }
        });

        // non-blocking
        oBob.subscribe(v -> System.out.println("onNext: " + v));
    }

    /**
     *
     */
    @Test
    void testSynchronous()
    {
        // execute() = queue().get()
        assertEquals("Hello World!", new CommandHelloWorld("World").execute());
        assertEquals("Hello Bob!", new CommandHelloWorld("Bob").execute());
    }
}

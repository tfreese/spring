package de.freese.spring;

import java.util.concurrent.TimeUnit;

import jakarta.annotation.Resource;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Thomas Freese
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS) // Nur für Mode.AverageTime
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {
        // Fork multipliziert die Anzahl der Iterationen
        "-disablesystemassertions"})
@Threads(1)
public class MicroServiceBenchmark {
    /**
     * @author Thomas Freese
     */
    // @State(Scope.Benchmark) // Einen neuen SpringContext für jeden Benchmark.
    @State(Scope.Group) // Nur einen SpringContext für alle Benchmarks -> @Group("spring").
    public static class BenchmarkState {
        private final ConfigurableApplicationContext context;
        private final RestTemplate restTemplate;
        private final WebClient webClient;
        @Value("${server.port}")
        private int port;

        @Resource
        private RestTemplateBuilder restTemplateBuilder;

        @Resource
        private WebClient.Builder webClientBuilder;

        public BenchmarkState() {
            super();

            this.context = SpringApplication.run(MicroServiceApplication.class);

            autowireBean(this);

            this.restTemplate = this.restTemplateBuilder.rootUri("http://localhost:" + this.port).build();
            this.webClient = this.webClientBuilder.baseUrl("http://localhost:" + this.port).build();
        }

        @TearDown
        public void close() {
            this.context.close();
        }

        private void autowireBean(final Object bean) {
            final AutowireCapableBeanFactory factory = this.context.getAutowireCapableBeanFactory();
            factory.autowireBean(bean);
        }
    }

    @Benchmark
    @Group("spring") // Nur einen SpringContext für alle Benchmarks.
    public void benchmarkRestTemplate(final Blackhole blackhole, final BenchmarkState state) {
        final RestTemplate restTemplate = state.restTemplate;

        final String response = restTemplate.getForObject("/", String.class);

        blackhole.consume(response);
    }

    @Benchmark
    @Group("spring") // Nur einen SpringContext für alle Benchmarks.
    public void benchmarkWebClient(final Blackhole blackhole, final BenchmarkState state) {
        final WebClient webClient = state.webClient;

        // Erzeugt Fehler, da Connection im Benchmark bereits geschlossen ist.
        // webClient.get().uri("/").retrieve().bodyToMono(String.class).subscribe(blackhole::consume);

        final String response = webClient.get().uri("/").retrieve().bodyToMono(String.class).block();

        blackhole.consume(response);
    }
}

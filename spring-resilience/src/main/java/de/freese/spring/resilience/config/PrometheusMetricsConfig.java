package de.freese.spring.resilience.config;

/**
 * In Grafana können die Metriken über den Prometheus Job-Namen (z.B. "job_name: 'spring-resilience'") gefiltert/ausgelesen werden.
 *
 * @author Thomas Freese
 */
//@Configuration
public class PrometheusMetricsConfig {
    // private static class MyPrometheusNamingConvention extends PrometheusNamingConvention {
    //     private final String appName;
    //
    //     public MyPrometheusNamingConvention(final String appName) {
    //         super();
    //
    //         this.appName = Objects.requireNonNull(appName, "appName required");
    //     }
    //
    //     @Override
    //     public String name(final String name, final Meter.Type type, final String baseUnit) {
    //         return this.appName + "_" + super.name(name, type, baseUnit);
    //     }
    // }
    //
    // @Bean
    // MeterRegistryCustomizer<MeterRegistry> metricsConfig(@Value("${info.app.name}") final String appName) {
    //     return registry -> {
    //         registry.config().commonTags("appName", appName);
    //         // registry.config().namingConvention(new MyPrometheusNamingConvention(appName));
    //     };
    // }
}

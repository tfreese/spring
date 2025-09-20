// Created: 30.04.2020
package de.freese.spring;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * @author Thomas Freese
 */
public final class Benchmarks {
    static void main() throws Exception {
        // org.openjdk.jmh.Main.main(args);

        // Der Builder überschreibt die Klassen-Annotationen.
        // Benötigt befüllte target\classes\META-INF\BenchmarkList -> Rebuild vor der Ausführung notwendig.
        final Options options = new OptionsBuilder()
                .include(MicroServiceBenchmark.class.getSimpleName())
                //.addProfiler(GCProfiler.class)
                //.addProfiler(HotspotMemoryProfiler.class)
                .resultFormat(ResultFormatType.CSV)
                .result("/dev/null")
                .build();

        new Runner(options).run();
    }

    private Benchmarks() {
        super();
    }
}

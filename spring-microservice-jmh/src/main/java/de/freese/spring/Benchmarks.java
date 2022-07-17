// Created: 30.04.2020
package de.freese.spring;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * @author Thomas Freese
 */
public class Benchmarks
{
    /**
     * @param args String[]
     *
     * @throws Exception Falls was schiefgeht.
     */
    public static void main(final String[] args) throws Exception
    {
        // org.openjdk.jmh.Main.main(args);

        // Der Builder überschreibt die Klassen-Annotationen.
        // Benötigt befüllte target\classes\META-INF\BenchmarkList -> Rebuild vor der Ausführung notwendig.
        // @formatter:off
        Options options = new OptionsBuilder()
                .include(MicroServiceBenchmark.class.getSimpleName())
                //.addProfiler(GCProfiler.class)
                //.addProfiler(HotspotMemoryProfiler.class)
                .resultFormat(ResultFormatType.CSV)
                .result("/dev/null")
                .build()
                ;
        // @formatter:on

        new Runner(options).run();
    }
}

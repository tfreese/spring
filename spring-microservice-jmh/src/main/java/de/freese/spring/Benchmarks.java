/**
 * Created: 30.04.2020
 */

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
     * @throws Exception Falls was schief geht.
     */
    public static void main(final String[] args) throws Exception
    {
        // Der Builder überschreibt die Klassen-Annotationen.
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

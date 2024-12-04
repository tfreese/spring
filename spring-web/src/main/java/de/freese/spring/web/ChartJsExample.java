// Created: 14.06.2024
package de.freese.spring.web;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import software.xdev.chartjs.model.charts.BarChart;
import software.xdev.chartjs.model.charts.Chart;
import software.xdev.chartjs.model.data.BarData;
import software.xdev.chartjs.model.dataset.BarDataset;
import software.xdev.chartjs.model.options.BarOptions;

/**
 * @author Thomas Freese
 */
public final class ChartJsExample {
    public static void main(final String[] args) {
        final BarData data = new BarData()
                .addLabels("A", "B", "C")
                .addDataset(new BarDataset()
                        .setLabel("Dataset1")
                        .setData(1, 3, 2));

        createAndOpenTestFile(new BarChart()
                .setData(data)
                .setOptions(new BarOptions())
        );

        System.exit(0);
    }

    private static void createAndOpenTestFile(final Chart<?, ?, ?> chart) {
        try {
            final Path tmpFile;

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                final File file = Files.createTempFile("chart_test_", ".html").toFile();

                if (!file.setReadable(true, true)) {
                    throw new IllegalStateException("can not make file readable");
                }

                if (!file.setWritable(true, true)) {
                    throw new IllegalStateException("can not make file writable");
                }

                if (!file.setExecutable(true, true)) {
                    throw new IllegalStateException("can not make file executable");
                }

                tmpFile = file.toPath();
            }
            else {
                final FileAttribute<Set<PosixFilePermission>> attribute = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
                final Path tmpDir = Files.createTempDirectory("java_", attribute);
                tmpFile = Files.createTempFile(tmpDir, "chart_test_", ".html", attribute);
            }

            final String html = """
                    <!DOCTYPE html>
                    <html lang="en">
                        <head>
                            <meta charset="UTF-8">
                            <script src=https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.3.0/chart.umd.js></script>
                        </head>
                        <body>
                            <canvas id="c" style="border:1px solid #555";></canvas>
                            <script>
                                new Chart(document.getElementById("c").getContext("2d"), %s);
                            </script>
                        </body>
                    </html>
                    """.formatted(chart.toJson());

            Files.writeString(tmpFile, html, StandardCharsets.UTF_8);

            Desktop.getDesktop().browse(tmpFile.toUri());
        }
        catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private ChartJsExample() {
        super();
    }
}

package de.freese.spring.web.chart;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.faces.view.ViewScoped;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.stereotype.Component;
import software.xdev.chartjs.model.charts.LineChart;
import software.xdev.chartjs.model.color.RGBAColor;
import software.xdev.chartjs.model.data.LineData;
import software.xdev.chartjs.model.dataset.LineDataset;
import software.xdev.chartjs.model.options.LegendOptions;
import software.xdev.chartjs.model.options.LineOptions;
import software.xdev.chartjs.model.options.Plugins;
import software.xdev.chartjs.model.options.Title;
import software.xdev.chartjs.model.options.scale.Scales;
import software.xdev.chartjs.model.options.scale.cartesian.AbstractCartesianScaleOptions;
import software.xdev.chartjs.model.options.scale.cartesian.linear.LinearScaleOptions;
import software.xdev.chartjs.model.options.scale.cartesian.time.TimeScaleOptions;
import software.xdev.chartjs.model.options.scale.cartesian.time.TimeScaleTickOptions;

/**
 * @author Thomas Freese
 */
// @Named
@Component  // see faces-config.xml: el-resolver
@ViewScoped
public final class LineChartBean implements Serializable {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Serial
    private static final long serialVersionUID = 7763047176024276034L;

    @Resource
    private transient DataService dataService;

    private String lineChartJson;

    public String getLineChartJson() {
        return lineChartJson;
    }

    @PostConstruct
    public void init() {
        // x-Axis
        // final LinearScaleOptions xScale = new LinearScaleOptions()
        //         .setTitle(new AbstractCartesianScaleOptions.Title()
        //                 .setText("Time")
        //         );
        final TimeScaleOptions xScale = new TimeScaleOptions()
                .setTitle(new AbstractCartesianScaleOptions.Title()
                        .setText("Time"))
                .setTime(new TimeScaleOptions.TimeOptions()
                        .setParser("YYYY-MM-DD HH:mm:ss") // Date Format in String.
                        .setDisplayFormats(new TimeScaleOptions.DisplayFormats()
                                .setSecond("YYYY-MM-DD HH:mm:ss"))
                        .setUnit("second")
                )
                .setTicks(new TimeScaleTickOptions()
                        // .setSource("data") // Generates ticks from data (including labels from data {x|y} objects).
                        // .setSource("labels") // Generates ticks from user given labels ONLY.
                        .setSource("auto")
                );

        // y-Axis
        final LinearScaleOptions yScale = new LinearScaleOptions()
                .setTitle(new AbstractCartesianScaleOptions.Title()
                        .setText("Values"))
                .setBeginAtZero(true);

        // For LinearScaleOptions.
        // xScale.setType(null);
        // yScale.setType(null);

        final List<Map.Entry<LocalDateTime, Double>> chartData = dataService.getData();
        final List<Long> distances = new ArrayList<>();

        // Average Distance between DataPoints.
        for (int i = 1; i < chartData.size(); i++) {
            final Map.Entry<LocalDateTime, Double> entry = chartData.get(i);
            final Map.Entry<LocalDateTime, Double> lastEntry = chartData.get(i - 1);

            final Duration distance = Duration.between(lastEntry.getKey(), entry.getKey());
            distances.add(distance.toNanos());
        }

        final long averageDistance = (long) distances.stream().mapToLong(Long::longValue).map(Math::abs).average().orElse(0D);

        // Histogram
        // final Map<Long, Long> histogram = distances.stream()
        //         .collect(
        //                 Collectors.groupingBy(
        //                         Function.identity(),
        //                         Collectors.counting()
        //                 ));

        if (averageDistance > 0L) {
            // Insert Time-Gap with null.
            for (int i = 1; i < chartData.size(); i++) {
                final Map.Entry<LocalDateTime, Double> entry = chartData.get(i);
                final Map.Entry<LocalDateTime, Double> lastEntry = chartData.get(i - 1);

                if (entry.getValue() != null
                        && lastEntry.getValue() != null
                        && lastEntry.getKey().isBefore(entry.getKey().minusNanos(averageDistance))) {
                    chartData.add(i, new AbstractMap.SimpleEntry<>(entry.getKey().minusNanos(averageDistance), null));
                }
            }
        }

        final LineChart lineChart = new LineChart()
                .setData(new LineData()
                        .addDataset(new LineDataset()
                                .setLabel("My Dataset")
                                .setData(chartData.stream().map(Map.Entry::getValue).map(Number.class::cast).toList())
                                .setBorderColor(new RGBAColor(RGBAColor.random(), 1D))
                                .setLineTension(0.1F)
                                // .setSpanGaps(true)
                                .setFill(false)
                        )
                        .setLabels(chartData.stream().map(Map.Entry::getKey).map(DATE_TIME_FORMATTER::format).toList()))
                .setOptions(new LineOptions()
                        .setMaintainAspectRatio(false)
                        .setResponsive(true)
                        .setSpanGaps(false) // No Data no Line.
                        .setScales(new Scales()
                                .addScale(Scales.ScaleAxis.X, xScale)
                                .addScale(Scales.ScaleAxis.Y, yScale)
                        )
                        .setPlugins(new Plugins()
                                .setTitle(new Title()
                                        .setDisplay(true)
                                        .setText("Chart Title")
                                )
                                .setLegend(new LegendOptions()
                                        .setDisplay(true)
                                        .setPosition("right")
                                )
                        )
                );

        final ObjectWriter objectWriter =
                new ObjectMapper()
                        .setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY)
                        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                        .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
                        .setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE)
                        .setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE)
                        .setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.NONE)
                        .writerWithDefaultPrettyPrinter();
        // .forType(getClass());

        lineChart.setDefaultObjectWriter(objectWriter);

        lineChartJson = lineChart.toJson();

        System.out.println(lineChartJson);
    }
}

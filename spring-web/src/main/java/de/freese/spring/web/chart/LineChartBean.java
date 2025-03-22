package de.freese.spring.web.chart;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import software.xdev.chartjs.model.options.elements.Fill;
import software.xdev.chartjs.model.options.scale.Scales;
import software.xdev.chartjs.model.options.scale.cartesian.AbstractCartesianScaleOptions;
import software.xdev.chartjs.model.options.scale.cartesian.linear.LinearScaleOptions;

/**
 * @author Thomas Freese
 */
@Component  // siehe faces-config.xml: el-resolver
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
        // x-Achse
        final LinearScaleOptions xScale = new LinearScaleOptions()
                .setTitle(new AbstractCartesianScaleOptions.Title()
                        .setText("Data")
                );

        // y-Achse
        final LinearScaleOptions yScale = new LinearScaleOptions()
                .setBeginAtZero(true);

        // chartjs-java-model:2.3.0 Workaround
        xScale.setType(null);
        yScale.setType(null);

        final Map<LocalDateTime, Double> chartData = this.dataService.getData();

        final LineChart lineChart = new LineChart()
                .setData(new LineData()
                        .addDataset(new LineDataset()
                                .setLabel("My Dataset")
                                .setData(chartData.values().stream().map(Number.class::cast).toList())
                                .setBorderColor(new RGBAColor(RGBAColor.random(), 1D))
                                .setLineTension(0.25F)
                                .setFill(new Fill<>(false))
                        )
                        .setLabels(chartData.keySet().stream().map(DATE_TIME_FORMATTER::format).toList()))
                .setOptions(new LineOptions()
                        .setMaintainAspectRatio(false)
                        .setResponsive(true)
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
        // .forType(this.getClass());

        lineChart.setDefaultObjectWriter(objectWriter);

        lineChartJson = lineChart.toJson();

        System.out.println(lineChartJson);
    }
}

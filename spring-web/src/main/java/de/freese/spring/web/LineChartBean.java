package de.freese.spring.web;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.faces.view.ViewScoped;

import org.springframework.stereotype.Component;
import software.xdev.chartjs.model.charts.LineChart;
import software.xdev.chartjs.model.color.RGBAColor;
import software.xdev.chartjs.model.data.LineData;
import software.xdev.chartjs.model.dataset.LineDataset;
import software.xdev.chartjs.model.options.Legend;
import software.xdev.chartjs.model.options.LineOptions;
import software.xdev.chartjs.model.options.Plugins;
import software.xdev.chartjs.model.options.Title;
import software.xdev.chartjs.model.options.elements.Fill;
import software.xdev.chartjs.model.options.scale.Scales;
import software.xdev.chartjs.model.options.scale.cartesian.AbstractCartesianScaleOptions;
import software.xdev.chartjs.model.options.scale.cartesian.linear.LinearScaleOptions;
import software.xdev.chartjs.model.options.scale.cartesian.linear.LinearTickOptions;

/**
 * @author Thomas Freese
 */
@Component  // siehe faces-config.xml: el-resolver
@ViewScoped
public final class LineChartBean implements Serializable {
    @Serial
    private static final long serialVersionUID = 7763047176024276034L;

    @Resource
    private transient DataService dataService;

    private String lineChart;

    public String getLineChart() {
        return lineChart;
    }

    @PostConstruct
    public void init() {
        final Title title = new Title();
        title.setDisplay(true);
        title.setText("Line Chart Title");

        final Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition(Legend.Position.RIGHT);

        final Scales scales = new Scales();

        // X-Achse
        final AbstractCartesianScaleOptions.Title xTitle = new AbstractCartesianScaleOptions.Title();
        xTitle.setText("Data");

        final LinearScaleOptions xScale = new LinearScaleOptions();
        xScale.setTitle(xTitle);

        // Y-Achse
        final LinearTickOptions ticks = new LinearTickOptions();
        // ticks.setBeginAtZero(true);

        final LinearScaleOptions yScale = new LinearScaleOptions();
        yScale.setTicks(ticks);

        // Achsen registrieren
        scales.addScale(Scales.ScaleAxis.X, xScale);
        scales.addScale(Scales.ScaleAxis.Y, yScale);

        final Map<Number, Number> chartData = this.dataService.getLineChartData();

        this.lineChart = new LineChart()
                .setData(new LineData()
                        .addDataset(new LineDataset()
                                .setLabel("My First Dataset")
                                .setData(chartData.values())
                                .setBorderColor(new RGBAColor(RGBAColor.random(), 1D))
                                .setLineTension(0.5F)
                                .setFill(new Fill<>(false))
                        )
                        .setLabels(chartData.keySet().stream().map(Number::toString).toList()))
                .setOptions(new LineOptions()
                        .setMaintainAspectRatio(false)
                        .setResponsive(true)
                        .setScales(scales)
                        .setPlugins(new Plugins()
                                .setTitle(title)
                                .setLegend(legend))
                )
                .toJson();
    }
}

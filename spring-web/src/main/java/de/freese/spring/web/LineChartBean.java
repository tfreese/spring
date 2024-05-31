package de.freese.spring.web;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.faces.view.ViewScoped;

import org.springframework.stereotype.Component;
import software.xdev.chartjs.model.charts.LineChart;
import software.xdev.chartjs.model.color.Color;
import software.xdev.chartjs.model.data.LineData;
import software.xdev.chartjs.model.dataset.LineDataset;
import software.xdev.chartjs.model.options.Legend;
import software.xdev.chartjs.model.options.LineOptions;
import software.xdev.chartjs.model.options.Plugins;
import software.xdev.chartjs.model.options.Title;
import software.xdev.chartjs.model.options.elements.Fill;
import software.xdev.chartjs.model.options.scales.LinearScale;
import software.xdev.chartjs.model.options.scales.Scale;
import software.xdev.chartjs.model.options.scales.ScaleTitle;
import software.xdev.chartjs.model.options.scales.Scales;
import software.xdev.chartjs.model.options.ticks.LinearTicks;

/**
 * @author Thomas Freese
 */
@Component  // siehe faces-config.xml: el-resolver
@ViewScoped
public class LineChartBean implements Serializable {
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
        final ScaleTitle xTitle = new ScaleTitle();
        xTitle.setText("Data");

        final Scale<LinearTicks, LinearScale> xScale = new LinearScale();
        xScale.setTitle(xTitle);

        // Y-Achse
        final LinearTicks ticks = new LinearTicks();
        ticks.setBeginAtZero(true);

        final Scale<LinearTicks, LinearScale> yScale = new LinearScale();
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
                                .setBorderColor(Color.random())
                                .setLineTension(0.5F)
                                .setFill(new Fill<>(false)))
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

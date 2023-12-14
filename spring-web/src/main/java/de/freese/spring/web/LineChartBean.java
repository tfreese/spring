package de.freese.spring.web;

import java.awt.geom.Point2D;
import java.io.Serial;
import java.io.Serializable;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.faces.view.ViewScoped;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;
import org.primefaces.model.charts.optionconfig.title.Title;
import org.springframework.stereotype.Component;

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

    private LineChartModel lineChartModel;

    public LineChartModel getLineChartModel() {
        return lineChartModel;
    }

    @PostConstruct
    public void init() {
        final LineChartDataSet chartDataSet = new LineChartDataSet();
        chartDataSet.setLabel("My First Dataset");
        chartDataSet.setFill(false);
        chartDataSet.setBorderColor("rgb(75, 192, 192)");
        chartDataSet.setTension(0.0D);
        chartDataSet.setData(this.dataService.getPoints().stream().map(Point2D.Double::getY).map(Object.class::cast).toList());

        // x-Achse funktioniert bis jetzt nur mit Strings.
        final ChartData chartData = new ChartData();
        chartData.addChartDataSet(chartDataSet);
        chartData.setLabels(this.dataService.getPoints().stream().map(Point2D.Double::getX).map(x -> Double.toString(x)).toList());
        //        chartData.setLabels(List.of("January", "February", "March", "April", "May", "June", "July"));
        //        chartData.setLabels(List.of(1, 2, 3, 4, 5, 6));

        final LineChartOptions options = new LineChartOptions();
        options.setMaintainAspectRatio(false);

        final Title title = new Title();
        title.setDisplay(true);
        title.setText("Line Chart");
        options.setTitle(title);

        final Title subtitle = new Title();
        subtitle.setDisplay(true);
        subtitle.setText("Line Chart Subtitle");
        options.setSubtitle(subtitle);

        this.lineChartModel = new LineChartModel();
        this.lineChartModel.setOptions(options);
        this.lineChartModel.setData(chartData);
    }
}

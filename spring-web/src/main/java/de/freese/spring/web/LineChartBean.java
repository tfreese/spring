package de.freese.spring.web;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.faces.view.ViewScoped;

import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
@ViewScoped
public class LineChartBean
{
    @Resource
    private DataService dataService;

    private LineChartModel lineModel;

    public LineChartModel getLineModel()
    {
        return lineModel;
    }

    @PostConstruct
    public void init()
    {
        lineModel = new LineChartModel();
        LineChartSeries s = new LineChartSeries();
        s.setLabel("Population");

        dataService.getLineChartData().forEach(s::set);

        lineModel.addSeries(s);
        lineModel.setLegendPosition("e");
        Axis y = lineModel.getAxis(AxisType.Y);
        y.setMin(0.5);
        y.setMax(700);
        y.setLabel("Millions");

        Axis x = lineModel.getAxis(AxisType.X);
        x.setMin(0);
        x.setMax(7);
        x.setTickInterval("1");
        x.setLabel("Number of Years");
    }
}

package de.freese.spring.web;

import java.awt.Point;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.faces.view.ViewScoped;

import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component  // siehe faces-config.xml: el-resolver
@ViewScoped
public class DataBean
{
    @Resource
    private DataService dataService;

    private String localDateTimeFormatted;

    public LocalDateTime getLocalDateTime()
    {
        return dataService.getLocalDateTime();
    }

    public String getLocalDateTimeFormatted()
    {
        return this.localDateTimeFormatted;
    }

    public List<Point.Double> getPoints()
    {
        return dataService.getPoints();
    }

    @PostConstruct
    public void init()
    {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");
        this.localDateTimeFormatted = dateTimeFormatter.format(getLocalDateTime());
    }
}
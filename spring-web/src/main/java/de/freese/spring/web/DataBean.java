package de.freese.spring.web;

import java.awt.geom.Point2D;
import java.io.Serial;
import java.io.Serializable;
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
public class DataBean implements Serializable {
    @Serial
    private static final long serialVersionUID = 3966368804680062710L;

    @Resource
    private transient DataService dataService;

    private String localDateTimeFormatted;

    public LocalDateTime getLocalDateTime() {
        return dataService.getLocalDateTime();
    }

    public String getLocalDateTimeFormatted() {
        return this.localDateTimeFormatted;
    }

    public List<Point2D.Double> getPoints() {
        return dataService.getPoints();
    }

    @PostConstruct
    public void init() {
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");
        this.localDateTimeFormatted = dateTimeFormatter.format(getLocalDateTime());
    }
}

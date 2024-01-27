package de.freese.spring.web;

import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

/**
 * @author Thomas Freese
 */
@Service
public class DataService {
    public Map<Number, Double> getLineChartData() {
        return getPoints().stream().collect(Collectors.toMap(Point2D.Double::getX, Point2D.Double::getY, (a, b) -> b, LinkedHashMap::new));
    }

    public LocalDateTime getLocalDateTime() {
        return LocalDateTime.now();
    }

    public List<Point2D.Double> getPoints() {
        final List<Point2D.Double> map = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            map.add(new Point2D.Double(i, Math.random() * 100D));
        }

        return map;
    }
}

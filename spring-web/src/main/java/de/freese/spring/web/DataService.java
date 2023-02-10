package de.freese.spring.web;

import java.awt.Point;
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
        //        Map<Double, Double> map = new LinkedHashMap<>();
        //        map.put(1, 5.20D);
        //        map.put(2, 19.63D);
        //        map.put(3, 59.01D);
        //        map.put(4, 139.76D);
        //        map.put(5, 300.4D);
        //        map.put(6, 630.0D);
        //
        //        return map;

        return getPoints().stream().collect(Collectors.toMap(Point2D.Double::getX, Point2D.Double::getY, (a, b) -> b, LinkedHashMap::new));
    }

    public LocalDateTime getLocalDateTime() {
        return LocalDateTime.now();
    }

    public List<Point.Double> getPoints() {
        List<Point.Double> map = new ArrayList<>();
        map.add(new Point2D.Double(1, 5.20D));
        map.add(new Point2D.Double(2, 19.63D));
        map.add(new Point2D.Double(3, 59.01D));
        map.add(new Point2D.Double(4, 139.76D));
        map.add(new Point2D.Double(5, 300.4D));
        map.add(new Point2D.Double(6, 630.0D));

        return map;
    }
}

package de.freese.spring.web.chart;

import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

/**
 * @author Thomas Freese
 */
@Service
public final class DataService {
    @Resource
    private Random random;

    public List<Map.Entry<LocalDateTime, Double>> getData() {

        final List<Map.Entry<LocalDateTime, Double>> data = new ArrayList<>();

        LocalDateTime localDateTime = getLocalDateTime();
        data.add(new AbstractMap.SimpleEntry<>(localDateTime, random.nextDouble(100D)));

        localDateTime = localDateTime.plusSeconds(1L);
        data.add(new AbstractMap.SimpleEntry<>(localDateTime, random.nextDouble(100D)));

        localDateTime = localDateTime.plusSeconds(1L);
        localDateTime = localDateTime.plusSeconds(1L);

        localDateTime = localDateTime.plusSeconds(1L);
        data.add(new AbstractMap.SimpleEntry<>(localDateTime, random.nextDouble(100D)));

        localDateTime = localDateTime.plusSeconds(1L);
        data.add(new AbstractMap.SimpleEntry<>(localDateTime, random.nextDouble(100D)));

        localDateTime = localDateTime.plusSeconds(1L);
        localDateTime = localDateTime.plusSeconds(1L);

        localDateTime = localDateTime.plusSeconds(1L);
        data.add(new AbstractMap.SimpleEntry<>(localDateTime, random.nextDouble(100D)));

        localDateTime = localDateTime.plusSeconds(1L);
        data.add(new AbstractMap.SimpleEntry<>(localDateTime, random.nextDouble(100D)));

        return data;
    }

    public LocalDateTime getLocalDateTime() {
        return LocalDateTime.now();
    }
}

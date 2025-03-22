package de.freese.spring.web.chart;

import java.time.LocalDateTime;
import java.util.HashMap;
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

    public Map<LocalDateTime, Double> getData() {
        final Map<LocalDateTime, Double> map = new HashMap<>();

        final LocalDateTime localDateTime = getLocalDateTime();

        for (int i = 0; i < 10; i++) {
            map.put(localDateTime.plusSeconds(i), random.nextDouble(100D));
        }

        return map;
    }

    public LocalDateTime getLocalDateTime() {
        return LocalDateTime.now();
    }
}

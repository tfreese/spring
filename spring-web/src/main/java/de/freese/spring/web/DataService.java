package de.freese.spring.web;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * @author Thomas Freese
 */
@Service
public final class DataService {
    public Map<LocalDateTime, Double> getData() {
        final Map<LocalDateTime, Double> map = new HashMap<>();

        final LocalDateTime localDateTime = getLocalDateTime();

        for (int i = 0; i < 10; i++) {
            map.put(localDateTime.plusSeconds(i), Math.random() * 100D);
        }

        return map;
    }

    public LocalDateTime getLocalDateTime() {
        return LocalDateTime.now();
    }
}

package de.freese.spring.web.chart;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

/**
 * @author Thomas Freese
 */
@Service
public final class DataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataService.class);

    @Resource
    private Random random;

    public List<Map.Entry<LocalDateTime, Double>> getData() {
        LOGGER.info("getData");

        LOGGER.atInfo().log("traceId={}", MDC.get("traceId"));
        LOGGER.atInfo().log("spanId={}", MDC.get("spanId"));
        // Span.current().getSpanContext().getTraceId();
        // Span.current().getSpanContext().getSpanId();
        // Span.current().getSpanContext().isValid();

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
        LOGGER.info("getLocalDateTime");

        return LocalDateTime.now(ZoneId.systemDefault());
    }
}

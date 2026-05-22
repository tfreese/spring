// Created: 22.05.2026
package de.spring.jooq;

import java.io.Serial;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.jooq.ExecuteType;

/**
 * <a href="https://www.jooq.org/doc/latest/manual/sql-execution/execute-listeners>execute-listeners</a>
 *
 * @author Thomas Freese
 */
public final class StatisticsListener implements ExecuteListener {
    private static final Map<ExecuteType, Integer> STATISTICS = new ConcurrentHashMap<>();

    @Serial
    private static final long serialVersionUID = -6470008201162077861L;

    public static int getOrDefault(final ExecuteType executeType, final int defaultValue) {
        return STATISTICS.getOrDefault(executeType, defaultValue);
    }

    @Override
    public void start(final ExecuteContext ctx) {
        STATISTICS.compute(ctx.type(), (k, v) -> v == null ? 1 : v + 1);
    }
}

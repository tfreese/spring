package de.spring.ai.tools;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Agend for simple Datetime functions.
 *
 * @author Thomas Freese
 */
public final class DateTimeTools {
    private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeTools.class);

    @Tool(description = "It returns the current date and time in the user's timezone.")
    String getCurrentDateTime() {
        LOGGER.info("Get current Time");

        return getDateTime(LocaleContextHolder.getTimeZone().toZoneId());
    }

    @Tool(description = """
            It returns the current date and time in the provided timezone.
            If the timezone is missing, use Europe/Berlin.
            """)
    String getDateTime(@ToolParam(description = "The timezone") final ZoneId zoneId) {
        LOGGER.info("Get Time from zone {}", zoneId);

        return ZonedDateTime.now(zoneId).toLocalDateTime().toString();
    }
}

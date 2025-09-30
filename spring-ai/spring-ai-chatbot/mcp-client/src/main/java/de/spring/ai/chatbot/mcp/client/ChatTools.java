package de.spring.ai.chatbot.mcp.client;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * @author Thomas Freese
 */
public final class ChatTools {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatTools.class);

    /**
     * In the user's timezone.
     */
    @Tool(name = "current_date_and_time", description = "It returns the current date and time in the user's timezone")
    String getCurrentDateTime() {
        LOGGER.info("Get current Time");
        
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }
}

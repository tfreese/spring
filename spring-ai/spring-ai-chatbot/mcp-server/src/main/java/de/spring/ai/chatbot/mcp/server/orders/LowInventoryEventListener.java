package de.spring.ai.chatbot.mcp.server.orders;

import de.spring.ai.chatbot.mcp.server.inventory.LowInventoryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
public class LowInventoryEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(LowInventoryEventListener.class);

    @EventListener
    public void handleLowInventoryEvent(final LowInventoryEvent event) {
        LOGGER.warn("Low Inventory: {}", event);
    }

    // @EventListener
    // public void handleOtherEvent(final OtherEvent event) {
    //     LOGGER.warn("{}", event);
    // }
}

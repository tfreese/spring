package de.spring.ai.chatbot.mcp.server.inventory;

/**
 * @author Thomas Freese
 */
public record LowInventoryEvent(
        long productId,
        long inventoryId,
        int currentAvailability,
        int requestedQuantity
) {
}

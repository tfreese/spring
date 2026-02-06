package de.spring.ai.mcp.server.inventory;

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

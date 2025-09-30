package de.spring.ai.chatbot.mcp.server.inventory;

/**
 * @author Thomas Freese
 */
public record InventoryData(
        long id,
        long productId,
        int availability) {
}

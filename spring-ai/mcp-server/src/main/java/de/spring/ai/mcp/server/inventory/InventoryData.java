package de.spring.ai.mcp.server.inventory;

/**
 * @author Thomas Freese
 */
public record InventoryData(long id, long productId, int availability) {
}

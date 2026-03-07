package de.spring.ai.chatbot.mcp.server.inventory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

/**
 * @author Thomas Freese
 */
@Repository
public class InventoryRepository {
    private final Map<Long, InventoryData> inventoryBackend = new ConcurrentHashMap<>();

    public InventoryData insert(final InventoryData inventoryData) {
        // final InventoryData storedInventoryData = restClient.post().uri("/api/inventory").body(inventoryData).retrieve().body(InventoryData.class);
        final InventoryData storedInventoryData = new InventoryData(inventoryBackend.size() + 1L, inventoryData.productId(), inventoryData.availability());

        inventoryBackend.put(storedInventoryData.id(), storedInventoryData);

        return storedInventoryData;
    }

    public List<InventoryData> selectAll() {
        // return restClient.get().uri("/api/inventory").retrieve().body(new ParameterizedTypeReference<>() {
        // });
        return List.copyOf(inventoryBackend.values());
    }

    public InventoryData selectById(final long inventoryId) {
        return inventoryBackend.get(inventoryId);
    }

    public void update(final InventoryData inventoryData) {
        inventoryBackend.put(inventoryData.id(), inventoryData);
    }
}

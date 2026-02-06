package de.spring.ai.mcp.server.inventory;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * <a href="https://dev.to/yuriybezsonov/a-practical-guide-to-building-ai-agents-with-java-and-spring-ai-part-5-add-mcp-4h2o">building-ai-agents</a>
 *
 * @author Thomas Freese
 */
@Service
public class InventoryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryService.class);

    private final ApplicationEventPublisher eventPublisher;
    private final InventoryRepository inventoryRepository;

    public InventoryService(final InventoryRepository inventoryRepository, final ApplicationEventPublisher eventPublisher) {
        super();

        this.inventoryRepository = Objects.requireNonNull(inventoryRepository, "inventoryRepository required");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher required");
    }

    @Tool(description = """
            Add a new product to the inventory item.
            If the product name is provided, then use the get all product tool to find the product id and then add it to the inventory.
            """)
    String addInventoryItem(@ToolParam(description = "The ID for a product") final int productId, @ToolParam(description = "The amount for a product") final int availability) {
        final InventoryData inventoryData = new InventoryData(0L, productId, availability);

        LOGGER.info("Adding inventory item: {}", inventoryData);

        final InventoryData inventoryDataFinal = inventoryRepository.insert(inventoryData);

        LOGGER.info("New inventory item added: {}", inventoryDataFinal);

        Objects.requireNonNull(inventoryDataFinal, "inventoryData required");

        return String.format("""
                Inventory item id: %d
                Product ID: %d
                Inventory Item availability: %d
                """, inventoryDataFinal.id(), inventoryDataFinal.productId(), inventoryDataFinal.availability());
    }

    @Tool(description = "Consume given item from the inventory")
    String consumeItem(@ToolParam(description = "The ID for an inventory item") final int inventoryId,
                       @ToolParam(description = "The Quantity for an inventory item") final int quantity) {
        LOGGER.info("Consuming inventory item");

        final InventoryData inventoryData = inventoryRepository.selectById(inventoryId);

        LOGGER.info("Inventory item loaded: {}", inventoryData);

        Objects.requireNonNull(inventoryData, "inventoryData required");

        final InventoryData updatedInventoryData = new InventoryData(inventoryId, inventoryData.productId(), inventoryData.availability() - quantity);
        inventoryRepository.update(updatedInventoryData);

        // Check if inventory will fall below 0 after consumption.
        if (inventoryData.availability() < quantity) {
            // Allow consumption but publish an event to trigger order placement.
            final LowInventoryEvent event = new LowInventoryEvent(
                    updatedInventoryData.productId(),
                    updatedInventoryData.id(),
                    updatedInventoryData.availability(),
                    quantity
            );

            eventPublisher.publishEvent(event);
        }

        // final InventoryData updatedInventoryData = restClient.put().uri(
        //         uriBuilder -> uriBuilder
        //                 .path(String.format("/api/inventory/%d/consume", inventoryId))
        //                 .queryParam("quantity", quantity)
        //                 .build()
        // ).retrieve().body(InventoryData.class);

        LOGGER.info("Inventory item changed: {}", updatedInventoryData);

        return toJson(updatedInventoryData);
    }

    @Tool(description = "It returns all the inventory items")
    String getAllInventoryItems() {
        LOGGER.info("Load all inventory items");

        return inventoryRepository.selectAll().stream()
                .map(this::toJson)
                .collect(Collectors.joining("\n"));
    }

    private String toJson(final InventoryData inventoryData) {
        final Function<InventoryData, String> inventoryDataStringFunction = pd -> """
                Inventory item ID: %d,
                Product ID: %d
                Inventory Item availability: %d
                """.formatted(inventoryData.id(), inventoryData.productId(), inventoryData.availability());

        return inventoryDataStringFunction.apply(inventoryData);
    }
}

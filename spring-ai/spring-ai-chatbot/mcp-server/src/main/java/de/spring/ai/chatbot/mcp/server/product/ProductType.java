package de.spring.ai.chatbot.mcp.server.product;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author Thomas Freese
 */
@Schema(description = "Types of products available in the inventory")
public enum ProductType {
    @Schema(description = "Meat products like chicken, beef, etc.")
    MEAT,

    @Schema(description = "Drink products like milk, juice, etc.")
    DRINK,

    @Schema(description = "Vegetable products like carrots, lettuce, etc.")
    VEGGIE,

    @Schema(description = "Desert products like ice cream, cake, etc.")
    DESERT
}

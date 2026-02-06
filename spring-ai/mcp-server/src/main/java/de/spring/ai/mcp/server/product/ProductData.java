package de.spring.ai.mcp.server.product;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Thomas Freese
 */
public record ProductData(
        @JsonProperty("id")
        long id,

        @JsonProperty("name")
        String name,

        @JsonProperty("type")
        ProductType type,

        @JsonProperty("price")
        double price
) {
}

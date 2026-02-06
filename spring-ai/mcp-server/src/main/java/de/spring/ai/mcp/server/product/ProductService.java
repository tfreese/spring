package de.spring.ai.mcp.server.product;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * <a href="https://dev.to/yuriybezsonov/a-practical-guide-to-building-ai-agents-with-java-and-spring-ai-part-5-add-mcp-4h2o">building-ai-agents</a><br>
 * <br>
 * Agend for a simple ProductStore.
 *
 * @author Thomas Freese
 */
@Service
public class ProductService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(final ProductRepository productRepository) {
        super();

        this.productRepository = Objects.requireNonNull(productRepository, "productRepository required");
    }

    /**
     * Prompt for confirmation before adding the product. -> Requires a confirmation in the Prompt: "Yes I will add it."
     */
    @Tool(description = """
            Adds a new product to the product store.
            Requires the product name, type and a valid price greater 0.
            Prompt for confirmation before adding the product.
            """)
    public String addProduct(@ToolParam(description = "The Name for a product") final String name,
                             @ToolParam(description = "The Type of product") final ProductType type,
                             @ToolParam(description = "The Price for a product") final double price) {
        final ProductData productData = new ProductData(0L, name, type, price);

        LOGGER.info("Attempting to add product. Raw input productsData: {}", productData);

        if (productData.name() == null || productData.name().isBlank()) {
            return "Product name is required to add a product.";
        }

        if (productData.price() <= 0D) {
            return "A valid price is required to add a product: " + productData.price();
        }

        final ProductData productDataFinal = Objects.requireNonNull(productRepository.insert(productData), "productData required");

        LOGGER.info("New product added: {}", productDataFinal);

        return productDataFinal.toString();
    }

    // @Tool(description = "It returns the name, type and cost of all available products.")
    // String getAllProducts() {
    //     LOGGER.info("Load all products");
    //
    //     return productRepository.selectAll()
    //             .map(this::toJson)
    //             .collect(Collectors.joining("\n"));
    // }

    /**
     * <pre>{@code
     *      @Tool(returnDirect = true) -> result passed back to the model.
     *      @Tool(returnDirect = false) -> result (JSON) returned directly.
     * }</pre>
     */
    @Tool(description = """
                It returns the name, type and cost of all available products.
                If the ProductType is missing, use all types.
            """)
    public String getProductsByType(@ToolParam(description = "The Type of product", required = false) @Nullable final ProductType type) {
        LOGGER.info("Load all products by type: {}", type);

        final ProductType productType = Optional.ofNullable(type).orElse(ProductType.ALL);

        return productRepository.selectAll(productType)
                .map(this::toJson)
                .collect(Collectors.joining("\n"));
    }

    private String toJson(final ProductData productData) {
        final Function<ProductData, String> productDataStringFunction = pd -> """
                Product ID: %d,
                Name: %s
                Type: %s
                Price: %.02f
                """.formatted(pd.id(), pd.name(), pd.type(), pd.price());

        return productDataStringFunction.apply(productData);
    }
}

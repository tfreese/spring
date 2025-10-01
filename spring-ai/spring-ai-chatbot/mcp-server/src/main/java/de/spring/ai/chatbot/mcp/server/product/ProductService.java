package de.spring.ai.chatbot.mcp.server.product;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
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

    @Tool(name = "add_product",
            description = "Adds a new product to the product table. Requires the product's name and a valid price. Prompt for confirmation before adding the product")
    // public String addProduct(ProductsData productsData) { // This is not working (converting json to object) - Instead use them as individual parameter.
    public String addProduct(@ToolParam(description = "The name of a product") final String name,
                             @ToolParam(description = "The type of a product") final ProductType type,
                             @ToolParam(description = "The price of a product") final double price) {
        final ProductData productData = new ProductData(0L, name, type, price);

        LOGGER.info("Attempting to add product. Raw input productsData: {}", productData);

        if (productData.name() == null || productData.name().isBlank()) {
            return "Product name is required to add a product.";
        }

        if (productData.price() <= 0D) {
            return "A valid price is required to add a product.";
        }

        final ProductData productDataFinal = productRepository.insert(productData);

        LOGGER.info("New product added: {}", productDataFinal);

        Objects.requireNonNull(productDataFinal, "productData required");

        return productDataFinal.toString();
    }

    @Tool(name = "get_all_products", description = "It returns the name, type and cost of all the available products")
    public String getProducts() {
        LOGGER.info("Load all products");

        final Function<ProductData, String> toJsonFunction = pd ->
                """
                        Product Id: %d,
                        Name: %s
                        Type: %s
                        Price: %.02f
                        """.formatted(pd.id(), pd.name(), pd.type(), pd.price());

        return productRepository.selectAll().stream()
                .map(toJsonFunction)
                .collect(Collectors.joining("\n"));
    }
}

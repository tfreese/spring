package de.spring.ai.mcp.server.product;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * @author Thomas Freese
 */
@Repository
public class ProductRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductRepository.class);

    private final Map<Long, ProductData> productBackend = new ConcurrentHashMap<>();

    public ProductData insert(final ProductData productData) {
        LOGGER.info("ProductData insert called: {}", productData);

        final ProductData storedProductData = new ProductData(productBackend.size() + 1L, productData.name(), productData.type(), productData.price());

        productBackend.put(storedProductData.id(), storedProductData);

        return storedProductData;
    }

    public Stream<ProductData> selectAll() {
        return List.copyOf(productBackend.values()).stream();
    }

    /**
     *
     * @param type {@link ProductType} not null
     */
    public Stream<ProductData> selectAll(final ProductType type) {
        Objects.requireNonNull(type);

        if (ProductType.ALL.equals(type)) {
            return selectAll();
        }

        return selectAll().filter(pd -> type.equals(pd.type()));
    }

    @PostConstruct
    void init() {
        LOGGER.info("ProductRepository init called");

        insert(new ProductData(0L, "Steak", ProductType.MEAT, 2.50D));
        insert(new ProductData(0L, "Bier", ProductType.DRINK, 1.50D));
    }
}

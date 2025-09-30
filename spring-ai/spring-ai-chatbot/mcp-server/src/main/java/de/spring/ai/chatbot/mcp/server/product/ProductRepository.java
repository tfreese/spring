package de.spring.ai.chatbot.mcp.server.product;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

/**
 * @author Thomas Freese
 */
@Repository
public class ProductRepository {
    private final Map<Long, ProductData> productBackend = new ConcurrentHashMap<>();

    public ProductData insert(final ProductData productData) {
        // final ProductData storedProduct = restClient.post().uri("/api/products").body(productsData).retrieve().body(ProductsData.class);
        final ProductData storedProductData = new ProductData(productBackend.size() + 1L, productData.name(), productData.type(), productData.price());

        productBackend.put(storedProductData.id(), storedProductData);

        return storedProductData;
    }

    public List<ProductData> selectAll() {
        // return restClient.get().uri("/api/products").retrieve().body(new ParameterizedTypeReference<>() {
        // });
        return List.copyOf(productBackend.values());
    }
}

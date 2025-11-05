package org.pwte.example.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

@DisplayName("Product Domain Model Tests")
class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
    }

    @Test
    @DisplayName("Should create Product with default constructor")
    void testDefaultConstructor() {
        Product newProduct = new Product();
        assertThat(newProduct).isNotNull();
    }

    @Test
    @DisplayName("Should set and get productId")
    void testProductId() {
        product.setProductId(1001);
        assertThat(product.getProductId()).isEqualTo(1001);
    }

    @Test
    @DisplayName("Should set and get name")
    void testName() {
        product.setName("Laptop Computer");
        assertThat(product.getName()).isEqualTo("Laptop Computer");
    }

    @Test
    @DisplayName("Should set and get price")
    void testPrice() {
        BigDecimal price = new BigDecimal("999.99");
        product.setPrice(price);
        assertThat(product.getPrice()).isEqualTo(price);
        assertThat(product.getPrice()).isEqualByComparingTo("999.99");
    }

    @Test
    @DisplayName("Should handle zero price")
    void testZeroPrice() {
        product.setPrice(BigDecimal.ZERO);
        assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should handle high precision price")
    void testHighPrecisionPrice() {
        BigDecimal price = new BigDecimal("1234.567890");
        product.setPrice(price);
        assertThat(product.getPrice()).isEqualTo(price);
    }

    @Test
    @DisplayName("Should set and get description")
    void testDescription() {
        String description = "High performance laptop with 16GB RAM and 512GB SSD";
        product.setDescription(description);
        assertThat(product.getDescription()).isEqualTo(description);
    }

    @Test
    @DisplayName("Should handle empty description")
    void testEmptyDescription() {
        product.setDescription("");
        assertThat(product.getDescription()).isEmpty();
    }

    @Test
    @DisplayName("Should handle null description")
    void testNullDescription() {
        product.setDescription(null);
        assertThat(product.getDescription()).isNull();
    }

    @Test
    @DisplayName("Should set and get imagePath")
    void testImagePath() {
        product.setImagePath("/images/laptop.jpg");
        assertThat(product.getImagePath()).isEqualTo("/images/laptop.jpg");
    }

    @Test
    @DisplayName("Should set and get categories")
    void testCategories() {
        Collection<Category> categories = new ArrayList<>();
        
        Category cat1 = new Category();
        cat1.setCategoryID(1);
        cat1.setName("Electronics");
        
        Category cat2 = new Category();
        cat2.setCategoryID(2);
        cat2.setName("Computers");
        
        categories.add(cat1);
        categories.add(cat2);

        product.setCategories(categories);
        assertThat(product.getCategories()).hasSize(2);
        assertThat(product.getCategories()).contains(cat1, cat2);
    }

    @Test
    @DisplayName("Should handle empty categories collection")
    void testEmptyCategories() {
        Collection<Category> emptyCategories = new ArrayList<>();
        product.setCategories(emptyCategories);
        assertThat(product.getCategories()).isEmpty();
    }

    @Test
    @DisplayName("Should handle null categories")
    void testNullCategories() {
        product.setCategories(null);
        assertThat(product.getCategories()).isNull();
    }

    @Test
    @DisplayName("Should be Serializable")
    void testSerializable() {
        assertThat(product).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    @DisplayName("Should create complete product with all fields")
    void testCompleteProduct() {
        product.setProductId(5001);
        product.setName("MacBook Pro");
        product.setPrice(new BigDecimal("2499.99"));
        product.setDescription("Apple MacBook Pro 16-inch with M3 chip");
        product.setImagePath("/images/macbook-pro.jpg");

        Collection<Category> categories = new ArrayList<>();
        Category category = new Category();
        category.setCategoryID(10);
        category.setName("Laptops");
        categories.add(category);
        product.setCategories(categories);

        assertThat(product.getProductId()).isEqualTo(5001);
        assertThat(product.getName()).isEqualTo("MacBook Pro");
        assertThat(product.getPrice()).isEqualByComparingTo("2499.99");
        assertThat(product.getDescription()).contains("M3 chip");
        assertThat(product.getImagePath()).contains("macbook");
        assertThat(product.getCategories()).hasSize(1);
    }
}

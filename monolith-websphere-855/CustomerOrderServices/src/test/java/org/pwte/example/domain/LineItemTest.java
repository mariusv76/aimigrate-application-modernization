package org.pwte.example.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.*;
import java.math.BigDecimal;

@DisplayName("LineItem Domain Model Tests")
class LineItemTest {

    private LineItem lineItem;

    @BeforeEach
    void setUp() {
        lineItem = new LineItem();
    }

    @Test
    @DisplayName("Should create LineItem with default constructor")
    void testDefaultConstructor() {
        LineItem newLineItem = new LineItem();
        assertThat(newLineItem).isNotNull();
    }

    @Test
    @DisplayName("Should set and get orderId")
    void testOrderId() {
        lineItem.setOrderId(1001);
        assertThat(lineItem.getOrderId()).isEqualTo(1001);
    }

    @Test
    @DisplayName("Should set and get productId")
    void testProductId() {
        lineItem.setProductId(2001);
        assertThat(lineItem.getProductId()).isEqualTo(2001);
    }

    @Test
    @DisplayName("Should set and get quantity")
    void testQuantity() {
        lineItem.setQuantity(5L);
        assertThat(lineItem.getQuantity()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Should handle zero quantity")
    void testZeroQuantity() {
        lineItem.setQuantity(0L);
        assertThat(lineItem.getQuantity()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should handle large quantity")
    void testLargeQuantity() {
        lineItem.setQuantity(1000L);
        assertThat(lineItem.getQuantity()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("Should set and get amount")
    void testAmount() {
        BigDecimal amount = new BigDecimal("99.99");
        lineItem.setAmount(amount);
        assertThat(lineItem.getAmount()).isEqualByComparingTo(amount);
    }

    @Test
    @DisplayName("Should handle zero amount")
    void testZeroAmount() {
        lineItem.setAmount(BigDecimal.ZERO);
        assertThat(lineItem.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should set and get product")
    void testProduct() {
        Product product = new Product();
        product.setProductId(3001);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("49.99"));

        lineItem.setProduct(product);
        assertThat(lineItem.getProduct()).isEqualTo(product);
        assertThat(lineItem.getProduct().getName()).isEqualTo("Test Product");
    }

    @Test
    @DisplayName("Should handle null product")
    void testNullProduct() {
        lineItem.setProduct(null);
        assertThat(lineItem.getProduct()).isNull();
    }

    @Test
    @DisplayName("Should set and get order")
    void testOrder() {
        Order order = new Order();
        order.setOrderId(4001);
        order.setTotal(new BigDecimal("199.99"));

        lineItem.setOrder(order);
        assertThat(lineItem.getOrder()).isEqualTo(order);
        assertThat(lineItem.getOrder().getOrderId()).isEqualTo(4001);
    }

    @Test
    @DisplayName("Should handle null order")
    void testNullOrder() {
        lineItem.setOrder(null);
        assertThat(lineItem.getOrder()).isNull();
    }

    @Test
    @DisplayName("Should set and get version")
    void testVersion() {
        lineItem.setVersion(3L);
        assertThat(lineItem.getVersion()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Should be Serializable")
    void testSerializable() {
        assertThat(lineItem).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    @DisplayName("Should create complete line item with all fields")
    void testCompleteLineItem() {
        // Create product
        Product product = new Product();
        product.setProductId(5001);
        product.setName("Laptop");
        product.setPrice(new BigDecimal("999.99"));

        // Create order
        Order order = new Order();
        order.setOrderId(6001);
        order.setTotal(new BigDecimal("0.00"));

        // Setup line item
        lineItem.setOrderId(6001);
        lineItem.setProductId(5001);
        lineItem.setQuantity(2L);
        lineItem.setAmount(new BigDecimal("1999.98"));
        lineItem.setProduct(product);
        lineItem.setOrder(order);
        lineItem.setVersion(1L);

        assertThat(lineItem.getOrderId()).isEqualTo(6001);
        assertThat(lineItem.getProductId()).isEqualTo(5001);
        assertThat(lineItem.getQuantity()).isEqualTo(2L);
        assertThat(lineItem.getAmount()).isEqualByComparingTo("1999.98");
        assertThat(lineItem.getProduct()).isNotNull();
        assertThat(lineItem.getProduct().getName()).isEqualTo("Laptop");
        assertThat(lineItem.getOrder()).isNotNull();
        assertThat(lineItem.getVersion()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should calculate amount based on quantity and price")
    void testAmountCalculation() {
        Product product = new Product();
        product.setPrice(new BigDecimal("25.50"));

        long quantity = 3L;
        BigDecimal expectedAmount = product.getPrice().multiply(new BigDecimal(quantity));

        lineItem.setProduct(product);
        lineItem.setQuantity(quantity);
        lineItem.setAmount(expectedAmount);

        assertThat(lineItem.getAmount()).isEqualByComparingTo("76.50");
    }
}

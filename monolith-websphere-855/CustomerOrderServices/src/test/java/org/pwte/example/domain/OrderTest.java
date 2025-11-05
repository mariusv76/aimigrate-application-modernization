package org.pwte.example.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@DisplayName("Order Domain Model Tests")
class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
    }

    @Test
    @DisplayName("Should create Order with default constructor")
    void testDefaultConstructor() {
        Order newOrder = new Order();
        assertThat(newOrder).isNotNull();
    }

    @Test
    @DisplayName("Should set and get orderId")
    void testOrderId() {
        order.setOrderId(1001);
        assertThat(order.getOrderId()).isEqualTo(1001);
    }

    @Test
    @DisplayName("Should set and get total")
    void testTotal() {
        BigDecimal total = new BigDecimal("1234.56");
        order.setTotal(total);
        assertThat(order.getTotal()).isEqualByComparingTo(total);
    }

    @Test
    @DisplayName("Should handle zero total")
    void testZeroTotal() {
        order.setTotal(BigDecimal.ZERO);
        assertThat(order.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should set and get status")
    void testStatus() {
        order.setStatus(Order.Status.OPEN);
        assertThat(order.getStatus()).isEqualTo(Order.Status.OPEN);
    }

    @Test
    @DisplayName("Should handle all status values")
    void testAllStatusValues() {
        order.setStatus(Order.Status.OPEN);
        assertThat(order.getStatus()).isEqualTo(Order.Status.OPEN);

        order.setStatus(Order.Status.SUBMITTED);
        assertThat(order.getStatus()).isEqualTo(Order.Status.SUBMITTED);

        order.setStatus(Order.Status.SHIPPED);
        assertThat(order.getStatus()).isEqualTo(Order.Status.SHIPPED);

        order.setStatus(Order.Status.CLOSED);
        assertThat(order.getStatus()).isEqualTo(Order.Status.CLOSED);
    }

    @Test
    @DisplayName("Should set and get submittedTime")
    void testSubmittedTime() {
        Date now = new Date();
        order.setSubmittedTime(now);
        assertThat(order.getSubmittedTime()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should handle null submittedTime")
    void testNullSubmittedTime() {
        order.setSubmittedTime(null);
        assertThat(order.getSubmittedTime()).isNull();
    }

    @Test
    @DisplayName("Should set and get customer")
    void testCustomer() {
        ResidentialCustomer customer = new ResidentialCustomer();
        customer.setCustomerId(100);
        customer.setName("John Doe");

        order.setCustomer(customer);
        assertThat(order.getCustomer()).isEqualTo(customer);
        assertThat(order.getCustomer().getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should handle null customer")
    void testNullCustomer() {
        order.setCustomer(null);
        assertThat(order.getCustomer()).isNull();
    }

    @Test
    @DisplayName("Should set and get lineitems")
    void testLineitems() {
        Set<LineItem> lineItems = new HashSet<>();
        
        LineItem item1 = new LineItem();
        LineItem item2 = new LineItem();
        
        lineItems.add(item1);
        lineItems.add(item2);

        order.setLineitems(lineItems);
        assertThat(order.getLineitems()).hasSize(2);
        assertThat(order.getLineitems()).contains(item1, item2);
    }

    @Test
    @DisplayName("Should handle empty lineitems set")
    void testEmptyLineitems() {
        Set<LineItem> emptyLineItems = new HashSet<>();
        order.setLineitems(emptyLineItems);
        assertThat(order.getLineitems()).isEmpty();
    }

    @Test
    @DisplayName("Should set and get version for optimistic locking")
    void testVersion() {
        order.setVersion(5L);
        assertThat(order.getVersion()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Should handle zero version")
    void testZeroVersion() {
        order.setVersion(0L);
        assertThat(order.getVersion()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should be Serializable")
    void testSerializable() {
        assertThat(order).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    @DisplayName("Should create complete order with all fields")
    void testCompleteOrder() {
        // Create customer
        ResidentialCustomer customer = new ResidentialCustomer();
        customer.setCustomerId(200);
        customer.setName("Jane Smith");

        // Create order
        order.setOrderId(9001);
        order.setTotal(new BigDecimal("599.99"));
        order.setStatus(Order.Status.SUBMITTED);
        order.setSubmittedTime(new Date());
        order.setCustomer(customer);
        order.setVersion(1L);

        // Create line items
        Set<LineItem> lineItems = new HashSet<>();
        LineItem item = new LineItem();
        lineItems.add(item);
        order.setLineitems(lineItems);

        assertThat(order.getOrderId()).isEqualTo(9001);
        assertThat(order.getTotal()).isEqualByComparingTo("599.99");
        assertThat(order.getStatus()).isEqualTo(Order.Status.SUBMITTED);
        assertThat(order.getSubmittedTime()).isNotNull();
        assertThat(order.getCustomer()).isNotNull();
        assertThat(order.getCustomer().getName()).isEqualTo("Jane Smith");
        assertThat(order.getLineitems()).hasSize(1);
        assertThat(order.getVersion()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should verify Status enum values")
    void testStatusEnum() {
        Order.Status[] statuses = Order.Status.values();
        assertThat(statuses).hasSize(4);
        assertThat(statuses).contains(
            Order.Status.OPEN,
            Order.Status.SUBMITTED,
            Order.Status.SHIPPED,
            Order.Status.CLOSED
        );
    }
}

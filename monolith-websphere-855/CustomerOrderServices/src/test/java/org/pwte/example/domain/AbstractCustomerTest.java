package org.pwte.example.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;

@DisplayName("AbstractCustomer Base Class Tests")
class AbstractCustomerTest {

    // Concrete implementation for testing abstract class
    private static class TestCustomer extends AbstractCustomer {
        public TestCustomer() {
            super();
        }
    }

    private TestCustomer customer;

    @BeforeEach
    void setUp() {
        customer = new TestCustomer();
    }

    @Test
    @DisplayName("Should create customer with default constructor")
    void testDefaultConstructor() {
        TestCustomer newCustomer = new TestCustomer();
        assertThat(newCustomer).isNotNull();
    }

    @Test
    @DisplayName("Should set and get customerId")
    void testCustomerId() {
        customer.setCustomerId(1001);
        assertThat(customer.getCustomerId()).isEqualTo(1001);
    }

    @Test
    @DisplayName("Should set and get name")
    void testName() {
        customer.setName("Test Customer");
        assertThat(customer.getName()).isEqualTo("Test Customer");
    }

    @Test
    @DisplayName("Should handle null name")
    void testNullName() {
        customer.setName(null);
        assertThat(customer.getName()).isNull();
    }

    @Test
    @DisplayName("Should set and get type")
    void testType() {
        customer.setType("BUSINESS");
        assertThat(customer.getType()).isEqualTo("BUSINESS");
    }

    @Test
    @DisplayName("Should set and get user")
    void testUser() {
        customer.setUser("testuser@example.com");
        assertThat(customer.getUser()).isEqualTo("testuser@example.com");
    }

    @Test
    @DisplayName("Should set and get address")
    void testAddress() {
        Address address = new Address();
        address.setAddressLine1("123 Test Street");
        address.setCity("Test City");
        address.setState("TC");
        address.setZip("12345");
        address.setCountry("USA");

        customer.setAddress(address);
        assertThat(customer.getAddress()).isNotNull();
        assertThat(customer.getAddress().getCity()).isEqualTo("Test City");
        assertThat(customer.getAddress().getZip()).isEqualTo("12345");
    }

    @Test
    @DisplayName("Should handle null address")
    void testNullAddress() {
        customer.setAddress(null);
        assertThat(customer.getAddress()).isNull();
    }

    @Test
    @DisplayName("Should set and get openOrder")
    void testOpenOrder() {
        Order openOrder = new Order();
        openOrder.setOrderId(5001);
        openOrder.setStatus(Order.Status.OPEN);

        customer.setOpenOrder(openOrder);
        assertThat(customer.getOpenOrder()).isNotNull();
        assertThat(customer.getOpenOrder().getOrderId()).isEqualTo(5001);
        assertThat(customer.getOpenOrder().getStatus()).isEqualTo(Order.Status.OPEN);
    }

    @Test
    @DisplayName("Should handle null openOrder")
    void testNullOpenOrder() {
        customer.setOpenOrder(null);
        assertThat(customer.getOpenOrder()).isNull();
    }

    @Test
    @DisplayName("Should set and get orders collection")
    void testOrders() {
        Set<Order> orders = new HashSet<>();
        
        Order order1 = new Order();
        order1.setOrderId(1);
        order1.setStatus(Order.Status.SUBMITTED);
        
        Order order2 = new Order();
        order2.setOrderId(2);
        order2.setStatus(Order.Status.SHIPPED);
        
        orders.add(order1);
        orders.add(order2);

        customer.setOrders(orders);
        assertThat(customer.getOrders()).hasSize(2);
        assertThat(customer.getOrders()).contains(order1, order2);
    }

    @Test
    @DisplayName("Should handle empty orders collection")
    void testEmptyOrders() {
        Set<Order> emptyOrders = new HashSet<>();
        customer.setOrders(emptyOrders);
        assertThat(customer.getOrders()).isEmpty();
    }

    @Test
    @DisplayName("Should handle null orders")
    void testNullOrders() {
        customer.setOrders(null);
        assertThat(customer.getOrders()).isNull();
    }

    @Test
    @DisplayName("Should be Serializable")
    void testSerializable() {
        assertThat(customer).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    @DisplayName("Should create complete customer with all fields")
    void testCompleteCustomer() {
        customer.setCustomerId(9001);
        customer.setName("Complete Test Customer");
        customer.setType("TEST");
        customer.setUser("complete@test.com");

        Address address = new Address();
        address.setAddressLine1("999 Complete Ave");
        address.setCity("Complete City");
        address.setState("CC");
        address.setZip("99999");
        address.setCountry("USA");
        customer.setAddress(address);

        Order openOrder = new Order();
        openOrder.setOrderId(1001);
        openOrder.setStatus(Order.Status.OPEN);
        customer.setOpenOrder(openOrder);

        Set<Order> orders = new HashSet<>();
        orders.add(openOrder);
        customer.setOrders(orders);

        assertThat(customer.getCustomerId()).isEqualTo(9001);
        assertThat(customer.getName()).isEqualTo("Complete Test Customer");
        assertThat(customer.getType()).isEqualTo("TEST");
        assertThat(customer.getUser()).isEqualTo("complete@test.com");
        assertThat(customer.getAddress()).isNotNull();
        assertThat(customer.getAddress().getCity()).isEqualTo("Complete City");
        assertThat(customer.getOpenOrder()).isNotNull();
        assertThat(customer.getOrders()).hasSize(1);
    }

    @Test
    @DisplayName("Should support customer with multiple historical orders")
    void testMultipleOrders() {
        Set<Order> orders = new HashSet<>();
        for (int i = 1; i <= 5; i++) {
            Order order = new Order();
            order.setOrderId(i);
            order.setStatus(i == 5 ? Order.Status.OPEN : Order.Status.CLOSED);
            orders.add(order);
        }

        customer.setOrders(orders);
        assertThat(customer.getOrders()).hasSize(5);
    }
}

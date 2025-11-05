package org.pwte.example.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.*;

@DisplayName("ResidentialCustomer Domain Model Tests")
class ResidentialCustomerTest {

    private ResidentialCustomer customer;

    @BeforeEach
    void setUp() {
        customer = new ResidentialCustomer();
    }

    @Test
    @DisplayName("Should create ResidentialCustomer with default constructor")
    void testDefaultConstructor() {
        ResidentialCustomer newCustomer = new ResidentialCustomer();
        assertThat(newCustomer).isNotNull();
    }

    @Test
    @DisplayName("Should inherit from AbstractCustomer")
    void testInheritance() {
        assertThat(customer).isInstanceOf(AbstractCustomer.class);
    }

    @Test
    @DisplayName("Should set and get household size")
    void testHouseholdSize() {
        customer.setHouseholdSize((short) 4);
        assertThat(customer.getHouseholdSize()).isEqualTo((short) 4);
    }

    @Test
    @DisplayName("Should handle household size of 1")
    void testSingleHousehold() {
        customer.setHouseholdSize((short) 1);
        assertThat(customer.getHouseholdSize()).isEqualTo((short) 1);
    }

    @Test
    @DisplayName("Should handle large household size")
    void testLargeHousehold() {
        customer.setHouseholdSize((short) 10);
        assertThat(customer.getHouseholdSize()).isEqualTo((short) 10);
    }

    @Test
    @DisplayName("Should handle zero household size")
    void testZeroHousehold() {
        customer.setHouseholdSize((short) 0);
        assertThat(customer.getHouseholdSize()).isEqualTo((short) 0);
    }

    @Test
    @DisplayName("Should set and get frequent customer as Y")
    void testFrequentCustomerYes() {
        customer.setFrequentCustomer("Y");
        assertThat(customer.isFrequentCustomer()).isTrue();
    }

    @Test
    @DisplayName("Should set and get frequent customer as N")
    void testFrequentCustomerNo() {
        customer.setFrequentCustomer("N");
        assertThat(customer.isFrequentCustomer()).isFalse();
    }

    @Test
    @DisplayName("Should handle null frequent customer")
    void testNullFrequentCustomer() {
        customer.setFrequentCustomer(null);
        assertThat(customer.isFrequentCustomer()).isFalse();
    }

    @Test
    @DisplayName("Should set and get inherited customer ID")
    void testInheritedCustomerId() {
        customer.setCustomerId(2001);
        assertThat(customer.getCustomerId()).isEqualTo(2001);
    }

    @Test
    @DisplayName("Should set and get inherited name")
    void testInheritedName() {
        customer.setName("John Doe");
        assertThat(customer.getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should set and get inherited type")
    void testInheritedType() {
        customer.setType("RESIDENTIAL");
        assertThat(customer.getType()).isEqualTo("RESIDENTIAL");
    }

    @Test
    @DisplayName("Should set and get inherited user")
    void testInheritedUser() {
        customer.setUser("john.doe@email.com");
        assertThat(customer.getUser()).isEqualTo("john.doe@email.com");
    }

    @Test
    @DisplayName("Should set and get inherited address")
    void testInheritedAddress() {
        Address address = new Address();
        address.setAddressLine1("123 Main Street");
        address.setCity("Portland");
        address.setState("OR");

        customer.setAddress(address);
        assertThat(customer.getAddress()).isNotNull();
        assertThat(customer.getAddress().getCity()).isEqualTo("Portland");
    }

    @Test
    @DisplayName("Should set and get inherited orders")
    void testInheritedOrders() {
        customer.setOrders(new java.util.HashSet<>());
        assertThat(customer.getOrders()).isNotNull();
        assertThat(customer.getOrders()).isEmpty();
    }

    @Test
    @DisplayName("Should set and get inherited open order")
    void testInheritedOpenOrder() {
        Order openOrder = new Order();
        openOrder.setOrderId(9001);
        openOrder.setStatus(Order.Status.OPEN);

        customer.setOpenOrder(openOrder);
        assertThat(customer.getOpenOrder()).isNotNull();
        assertThat(customer.getOpenOrder().getStatus()).isEqualTo(Order.Status.OPEN);
    }

    @Test
    @DisplayName("Should be Serializable")
    void testSerializable() {
        assertThat(customer).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    @DisplayName("Should create complete residential customer")
    void testCompleteResidentialCustomer() {
        customer.setCustomerId(3001);
        customer.setName("Jane Smith");
        customer.setType("RESIDENTIAL");
        customer.setUser("jane.smith@email.com");
        customer.setHouseholdSize((short) 3);
        customer.setFrequentCustomer("Y");

        Address address = new Address();
        address.setAddressLine1("789 Residential Lane");
        address.setCity("Austin");
        address.setState("TX");
        address.setZip("78701");
        address.setCountry("USA");
        customer.setAddress(address);

        assertThat(customer.getCustomerId()).isEqualTo(3001);
        assertThat(customer.getName()).isEqualTo("Jane Smith");
        assertThat(customer.getType()).isEqualTo("RESIDENTIAL");
        assertThat(customer.getHouseholdSize()).isEqualTo((short) 3);
        assertThat(customer.isFrequentCustomer()).isTrue();
        assertThat(customer.getAddress()).isNotNull();
        assertThat(customer.getAddress().getCity()).isEqualTo("Austin");
    }
}

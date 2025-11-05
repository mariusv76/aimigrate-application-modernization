package org.pwte.example.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.*;

@DisplayName("BusinessCustomer Domain Model Tests")
class BusinessCustomerTest {

    private BusinessCustomer customer;

    @BeforeEach
    void setUp() {
        customer = new BusinessCustomer();
    }

    @Test
    @DisplayName("Should create BusinessCustomer with default constructor")
    void testDefaultConstructor() {
        BusinessCustomer newCustomer = new BusinessCustomer();
        assertThat(newCustomer).isNotNull();
    }

    @Test
    @DisplayName("Should inherit from AbstractCustomer")
    void testInheritance() {
        assertThat(customer).isInstanceOf(AbstractCustomer.class);
    }

    @Test
    @DisplayName("Should set and get volume discount as Y")
    void testVolumeDiscountYes() {
        customer.setVolumeDiscount("Y");
        assertThat(customer.isVolumeDiscount()).isTrue();
    }

    @Test
    @DisplayName("Should set and get volume discount as N")
    void testVolumeDiscountNo() {
        customer.setVolumeDiscount("N");
        assertThat(customer.isVolumeDiscount()).isFalse();
    }

    @Test
    @DisplayName("Should handle null volume discount")
    void testNullVolumeDiscount() {
        customer.setVolumeDiscount(null);
        assertThat(customer.isVolumeDiscount()).isFalse();
    }

    @Test
    @DisplayName("Should set and get business partner as Y")
    void testBusinessPartnerYes() {
        customer.setBusinessPartner("Y");
        assertThat(customer.isBusinessPartner()).isTrue();
    }

    @Test
    @DisplayName("Should set and get business partner as N")
    void testBusinessPartnerNo() {
        customer.setBusinessPartner("N");
        assertThat(customer.isBusinessPartner()).isFalse();
    }

    @Test
    @DisplayName("Should handle null business partner")
    void testNullBusinessPartner() {
        customer.setBusinessPartner(null);
        assertThat(customer.isBusinessPartner()).isFalse();
    }

    @Test
    @DisplayName("Should set and get description")
    void testDescription() {
        String description = "Enterprise customer with bulk purchasing";
        customer.setDescription(description);
        assertThat(customer.getDescription()).isEqualTo(description);
    }

    @Test
    @DisplayName("Should handle null description")
    void testNullDescription() {
        customer.setDescription(null);
        assertThat(customer.getDescription()).isNull();
    }

    @Test
    @DisplayName("Should set and get inherited customer ID")
    void testInheritedCustomerId() {
        customer.setCustomerId(1001);
        assertThat(customer.getCustomerId()).isEqualTo(1001);
    }

    @Test
    @DisplayName("Should set and get inherited name")
    void testInheritedName() {
        customer.setName("Acme Corporation");
        assertThat(customer.getName()).isEqualTo("Acme Corporation");
    }

    @Test
    @DisplayName("Should set and get inherited type")
    void testInheritedType() {
        customer.setType("BUSINESS");
        assertThat(customer.getType()).isEqualTo("BUSINESS");
    }

    @Test
    @DisplayName("Should set and get inherited user")
    void testInheritedUser() {
        customer.setUser("admin@acme.com");
        assertThat(customer.getUser()).isEqualTo("admin@acme.com");
    }

    @Test
    @DisplayName("Should set and get inherited address")
    void testInheritedAddress() {
        Address address = new Address();
        address.setAddressLine1("123 Business Blvd");
        address.setCity("New York");
        address.setState("NY");

        customer.setAddress(address);
        assertThat(customer.getAddress()).isNotNull();
        assertThat(customer.getAddress().getCity()).isEqualTo("New York");
    }

    @Test
    @DisplayName("Should be Serializable")
    void testSerializable() {
        assertThat(customer).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    @DisplayName("Should create complete business customer")
    void testCompleteBusinessCustomer() {
        customer.setCustomerId(5001);
        customer.setName("Tech Solutions Inc");
        customer.setType("BUSINESS");
        customer.setUser("contact@techsolutions.com");
        customer.setVolumeDiscount("Y");
        customer.setBusinessPartner("Y");
        customer.setDescription("Large IT services provider");

        Address address = new Address();
        address.setAddressLine1("456 Enterprise Way");
        address.setCity("Seattle");
        address.setState("WA");
        address.setZip("98101");
        address.setCountry("USA");
        customer.setAddress(address);

        assertThat(customer.getCustomerId()).isEqualTo(5001);
        assertThat(customer.getName()).isEqualTo("Tech Solutions Inc");
        assertThat(customer.getType()).isEqualTo("BUSINESS");
        assertThat(customer.isVolumeDiscount()).isTrue();
        assertThat(customer.isBusinessPartner()).isTrue();
        assertThat(customer.getDescription()).contains("IT services");
        assertThat(customer.getAddress()).isNotNull();
        assertThat(customer.getAddress().getCity()).isEqualTo("Seattle");
    }
}

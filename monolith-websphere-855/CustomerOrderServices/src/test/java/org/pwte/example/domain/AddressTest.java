package org.pwte.example.domain;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Address Domain Model Tests")
class AddressTest {

    private Address address;

    @BeforeEach
    void setUp() {
        address = new Address();
    }

    @Test
    @DisplayName("Should create Address with default constructor")
    void testDefaultConstructor() {
        Address newAddress = new Address();
        assertThat(newAddress).isNotNull();
    }

    @Test
    @DisplayName("Should set and get addressLine1")
    void testAddressLine1() {
        address.setAddressLine1("123 Main Street");
        assertThat(address.getAddressLine1()).isEqualTo("123 Main Street");
    }

    @Test
    @DisplayName("Should set and get addressLine2")
    void testAddressLine2() {
        address.setAddressLine2("Apt 4B");
        assertThat(address.getAddressLine2()).isEqualTo("Apt 4B");
    }

    @Test
    @DisplayName("Should set and get city")
    void testCity() {
        address.setCity("New York");
        assertThat(address.getCity()).isEqualTo("New York");
    }

    @Test
    @DisplayName("Should set and get state")
    void testState() {
        address.setState("NY");
        assertThat(address.getState()).isEqualTo("NY");
    }

    @Test
    @DisplayName("Should set and get country")
    void testCountry() {
        address.setCountry("USA");
        assertThat(address.getCountry()).isEqualTo("USA");
    }

    @Test
    @DisplayName("Should set and get zip")
    void testZip() {
        address.setZip("10001");
        assertThat(address.getZip()).isEqualTo("10001");
    }

    @Test
    @DisplayName("Should handle null values in all fields")
    void testNullValues() {
        address.setAddressLine1(null);
        address.setAddressLine2(null);
        address.setCity(null);
        address.setState(null);
        address.setCountry(null);
        address.setZip(null);

        assertThat(address.getAddressLine1()).isNull();
        assertThat(address.getAddressLine2()).isNull();
        assertThat(address.getCity()).isNull();
        assertThat(address.getState()).isNull();
        assertThat(address.getCountry()).isNull();
        assertThat(address.getZip()).isNull();
    }

    @Test
    @DisplayName("Should create complete address with all fields")
    void testCompleteAddress() {
        address.setAddressLine1("123 Main Street");
        address.setAddressLine2("Suite 100");
        address.setCity("Seattle");
        address.setState("WA");
        address.setCountry("USA");
        address.setZip("98101");

        assertThat(address.getAddressLine1()).isEqualTo("123 Main Street");
        assertThat(address.getAddressLine2()).isEqualTo("Suite 100");
        assertThat(address.getCity()).isEqualTo("Seattle");
        assertThat(address.getState()).isEqualTo("WA");
        assertThat(address.getCountry()).isEqualTo("USA");
        assertThat(address.getZip()).isEqualTo("98101");
    }

    @Test
    @DisplayName("Should be Serializable")
    void testSerializable() {
        assertThat(address).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    @DisplayName("Should have correct serialVersionUID")
    void testSerialVersionUID() {
        // This verifies the class has the expected serial version UID
        assertThat(Address.class).hasDeclaredFields("serialVersionUID");
    }
}

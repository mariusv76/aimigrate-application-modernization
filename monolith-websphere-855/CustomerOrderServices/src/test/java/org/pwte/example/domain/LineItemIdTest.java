package org.pwte.example.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.*;

@DisplayName("LineItemId Composite Key Tests")
class LineItemIdTest {

    private LineItemId lineItemId;

    @BeforeEach
    void setUp() {
        lineItemId = new LineItemId();
    }

    @Test
    @DisplayName("Should create LineItemId with default constructor")
    void testDefaultConstructor() {
        LineItemId newId = new LineItemId();
        assertThat(newId).isNotNull();
    }

    @Test
    @DisplayName("Should set and get orderId")
    void testOrderId() {
        lineItemId.setOrderId(1001);
        assertThat(lineItemId.getOrderId()).isEqualTo(1001);
    }

    @Test
    @DisplayName("Should set and get productId")
    void testProductId() {
        lineItemId.setProductId(5001);
        assertThat(lineItemId.getProductId()).isEqualTo(5001);
    }

    @Test
    @DisplayName("Should implement equals correctly for same object")
    void testEqualsSameObject() {
        assertThat(lineItemId).isEqualTo(lineItemId);
    }

    @Test
    @DisplayName("Should implement equals correctly for equal IDs")
    void testEqualsEqualIDs() {
        LineItemId id1 = new LineItemId();
        id1.setOrderId(100);
        id1.setProductId(200);

        LineItemId id2 = new LineItemId();
        id2.setOrderId(100);
        id2.setProductId(200);

        assertThat(id1).isEqualTo(id2);
        assertThat(id2).isEqualTo(id1);
    }

    @Test
    @DisplayName("Should implement equals correctly for different order IDs")
    void testEqualsDifferentOrderId() {
        LineItemId id1 = new LineItemId();
        id1.setOrderId(100);
        id1.setProductId(200);

        LineItemId id2 = new LineItemId();
        id2.setOrderId(101);
        id2.setProductId(200);

        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    @DisplayName("Should implement equals correctly for different product IDs")
    void testEqualsDifferentProductId() {
        LineItemId id1 = new LineItemId();
        id1.setOrderId(100);
        id1.setProductId(200);

        LineItemId id2 = new LineItemId();
        id2.setOrderId(100);
        id2.setProductId(201);

        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    @DisplayName("Should handle equals with null")
    void testEqualsNull() {
        assertThat(lineItemId).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should handle equals with different class")
    void testEqualsDifferentClass() {
        assertThat(lineItemId).isNotEqualTo("Not a LineItemId");
    }

    @Test
    @DisplayName("Should implement hashCode correctly for equal objects")
    void testHashCodeEqualObjects() {
        LineItemId id1 = new LineItemId();
        id1.setOrderId(100);
        id1.setProductId(200);

        LineItemId id2 = new LineItemId();
        id2.setOrderId(100);
        id2.setProductId(200);

        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    @DisplayName("Should implement hashCode consistently")
    void testHashCodeConsistency() {
        lineItemId.setOrderId(100);
        lineItemId.setProductId(200);

        int hashCode1 = lineItemId.hashCode();
        int hashCode2 = lineItemId.hashCode();

        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    @DisplayName("Should be Serializable")
    void testSerializable() {
        assertThat(lineItemId).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    @DisplayName("Should create valid composite key")
    void testValidCompositeKey() {
        lineItemId.setOrderId(9001);
        lineItemId.setProductId(3001);

        assertThat(lineItemId.getOrderId()).isEqualTo(9001);
        assertThat(lineItemId.getProductId()).isEqualTo(3001);
        assertThat(lineItemId.hashCode()).isNotZero();
    }
}

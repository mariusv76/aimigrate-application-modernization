package org.pwte.example.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pwte.example.domain.*;
import org.pwte.example.exception.*;
import org.pwte.example.service.CustomerOrderServices;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerOrderResource REST endpoint.
 * Tests JAX-RS operations for customer order management.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerOrderResource REST API Tests")
class CustomerOrderResourceTest {

    @Mock
    private CustomerOrderServices customerOrderServices;

    @Mock
    private HttpHeaders httpHeaders;

    private CustomerOrderResource customerOrderResource;

    private static final int TEST_CUSTOMER_ID = 100;
    private static final int TEST_PRODUCT_ID = 200;
    private static final long TEST_VERSION = 5L;

    private ResidentialCustomer testCustomer;
    private Order testOrder;
    private LineItem testLineItem;
    private Address testAddress;

    @BeforeEach
    void setUp() throws Exception {
        // Create test customer
        testCustomer = new ResidentialCustomer();
        testCustomer.setCustomerId(TEST_CUSTOMER_ID);
        testCustomer.setUser("testuser");
        testCustomer.setName("Test User");

        // Create test order
        testOrder = new Order();
        testOrder.setOrderId(300);
        testOrder.setCustomer(testCustomer);
        testOrder.setStatus(Order.Status.OPEN);
        testOrder.setTotal(new BigDecimal("99.99"));
        testOrder.setVersion(TEST_VERSION);
        testOrder.setLineitems(new HashSet<>());

        // Create test line item
        testLineItem = new LineItem();
        testLineItem.setProductId(TEST_PRODUCT_ID);
        testLineItem.setQuantity(2L);
        testLineItem.setAmount(new BigDecimal("199.98"));

        // Create test address
        testAddress = new Address();
        testAddress.setAddressLine1("123 Test St");
        testAddress.setCity("Test City");
        testAddress.setState("TS");
        testAddress.setZip("12345");

        // Create resource with mocked service
        customerOrderResource = new CustomerOrderResource() {
            {
                // Inject mock service directly (bypass JNDI lookup)
                this.customerOrderServices = CustomerOrderResourceTest.this.customerOrderServices;
            }
        };
    }

    // ===== getCustomer Tests =====

    @Test
    @DisplayName("GET /Customer should return customer with open order")
    void testGetCustomer_WithOpenOrder() throws Exception {
        // Arrange
        testCustomer.setOpenOrder(testOrder);
        when(customerOrderServices.loadCustomer()).thenReturn(testCustomer);

        // Act
        Response response = customerOrderResource.getCustomer();

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(AbstractCustomer.class);
        
        AbstractCustomer result = (AbstractCustomer) response.getEntity();
        assertThat(result.getCustomerId()).isEqualTo(TEST_CUSTOMER_ID);
        
        // Verify ETag header is set with version
        assertThat(response.getMetadata().getFirst("ETag")).isEqualTo(TEST_VERSION);
        
        verify(customerOrderServices).loadCustomer();
    }

    @Test
    @DisplayName("GET /Customer should return customer without ETag when no open order")
    void testGetCustomer_WithoutOpenOrder() throws Exception {
        // Arrange
        testCustomer.setOpenOrder(null);
        when(customerOrderServices.loadCustomer()).thenReturn(testCustomer);

        // Act
        Response response = customerOrderResource.getCustomer();

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(AbstractCustomer.class);
        
        // Verify no ETag header when no open order
        assertThat(response.getMetadata().getFirst("ETag")).isNull();
        
        verify(customerOrderServices).loadCustomer();
    }

    @Test
    @DisplayName("GET /Customer should throw 404 when customer does not exist")
    void testGetCustomer_NotFound() throws Exception {
        // Arrange
        when(customerOrderServices.loadCustomer()).thenThrow(new CustomerDoesNotExistException());

        // Act & Assert
        assertThatThrownBy(() -> customerOrderResource.getCustomer())
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("GET /Customer should handle general persistence exception")
    void testGetCustomer_PersistenceException() throws Exception {
        // Arrange
        when(customerOrderServices.loadCustomer()).thenThrow(new GeneralPersistenceException());

        // Act & Assert
        assertThatThrownBy(() -> customerOrderResource.getCustomer())
                .isInstanceOf(WebApplicationException.class);
    }

    // ===== updateAddress Tests =====

    @Test
    @DisplayName("PUT /Customer/Address should update address successfully")
    void testUpdateAddress_Success() throws Exception {
        // Arrange
        doNothing().when(customerOrderServices).updateAddress(any(Address.class));

        // Act
        Response response = customerOrderResource.updateAddress(testAddress);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(customerOrderServices).updateAddress(testAddress);
    }

    @Test
    @DisplayName("PUT /Customer/Address should throw 404 when customer not found")
    void testUpdateAddress_CustomerNotFound() throws Exception {
        // Arrange
        doThrow(new CustomerDoesNotExistException())
                .when(customerOrderServices).updateAddress(any(Address.class));

        // Act & Assert
        assertThatThrownBy(() -> customerOrderResource.updateAddress(testAddress))
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("PUT /Customer/Address should handle general exceptions")
    void testUpdateAddress_GeneralException() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Database error"))
                .when(customerOrderServices).updateAddress(any(Address.class));

        // Act & Assert
        assertThatThrownBy(() -> customerOrderResource.updateAddress(testAddress))
                .isInstanceOf(WebApplicationException.class);
    }

    // ===== addLineItem Tests =====

    @Test
    @DisplayName("POST /Customer/OpenOrder/LineItem should add line item with If-Match header")
    void testAddLineItem_Success() throws Exception {
        // Arrange
        List<String> matchHeaders = Arrays.asList(String.valueOf(TEST_VERSION));
        when(httpHeaders.getRequestHeader("If-Match")).thenReturn(matchHeaders);
        when(customerOrderServices.addLineItem(any(LineItem.class))).thenReturn(testOrder);

        // Act
        Response response = customerOrderResource.addLineItem(testLineItem, httpHeaders);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(Order.class);
        assertThat(response.getMetadata().getFirst("ETag")).isEqualTo(TEST_VERSION);
        
        
        verify(customerOrderServices).addLineItem(any(LineItem.class));
    }

    @Test
    @DisplayName("POST /Customer/OpenOrder/LineItem should work without If-Match header")
    void testAddLineItem_NoIfMatchHeader() throws Exception {
        // Arrange
        when(httpHeaders.getRequestHeader("If-Match")).thenReturn(null);
        when(customerOrderServices.addLineItem(any(LineItem.class))).thenReturn(testOrder);

        // Act
        Response response = customerOrderResource.addLineItem(testLineItem, httpHeaders);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(customerOrderServices).addLineItem(testLineItem);
    }

    @Test
    @DisplayName("POST /Customer/OpenOrder/LineItem should throw 404 when customer not found")
    void testAddLineItem_CustomerNotFound() throws Exception {
        // Arrange
        when(httpHeaders.getRequestHeader("If-Match")).thenReturn(null);
        when(customerOrderServices.addLineItem(any(LineItem.class)))
                .thenThrow(new CustomerDoesNotExistException());

        // Act & Assert
        assertThatThrownBy(() -> customerOrderResource.addLineItem(testLineItem, httpHeaders))
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("POST /Customer/OpenOrder/LineItem should throw 404 when product not found")
    void testAddLineItem_ProductNotFound() throws Exception {
        // Arrange
        when(httpHeaders.getRequestHeader("If-Match")).thenReturn(null);
        when(customerOrderServices.addLineItem(any(LineItem.class)))
                .thenThrow(new ProductDoesNotExistException());

        // Act & Assert
        assertThatThrownBy(() -> customerOrderResource.addLineItem(testLineItem, httpHeaders))
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("POST /Customer/OpenOrder/LineItem should throw 400 for invalid quantity")
    void testAddLineItem_InvalidQuantity() throws Exception {
        // Arrange
        when(httpHeaders.getRequestHeader("If-Match")).thenReturn(null);
        when(customerOrderServices.addLineItem(any(LineItem.class)))
                .thenThrow(new InvalidQuantityException());

        // Act & Assert
        assertThatThrownBy(() -> customerOrderResource.addLineItem(testLineItem, httpHeaders))
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    @DisplayName("POST /Customer/OpenOrder/LineItem should throw 412 for version mismatch")
    void testAddLineItem_OrderModified() throws Exception {
        // Arrange
        when(httpHeaders.getRequestHeader("If-Match")).thenReturn(null);
        when(customerOrderServices.addLineItem(any(LineItem.class)))
                .thenThrow(new OrderModifiedException());

        // Act & Assert
        assertThatThrownBy(() -> customerOrderResource.addLineItem(testLineItem, httpHeaders))
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(Response.Status.PRECONDITION_FAILED.getStatusCode());
    }

    // ===== removeLineItem Tests =====

    @Test
    @DisplayName("DELETE /Customer/OpenOrder/LineItem/{productId} should remove line item")
    void testRemoveLineItem_Success() throws Exception {
        // Arrange
        List<String> matchHeaders = Arrays.asList(String.valueOf(TEST_VERSION));
        when(httpHeaders.getRequestHeader("If-Match")).thenReturn(matchHeaders);
        when(customerOrderServices.removeLineItem(eq(TEST_PRODUCT_ID), eq(TEST_VERSION)))
                .thenReturn(testOrder);

        // Act
        Response response = customerOrderResource.removeLineItem(TEST_PRODUCT_ID, httpHeaders);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(Order.class);
        assertThat(response.getMetadata().getFirst("ETag")).isEqualTo(TEST_VERSION);
        
        verify(customerOrderServices).removeLineItem(TEST_PRODUCT_ID, TEST_VERSION);
    }

    @Test
    @DisplayName("DELETE /Customer/OpenOrder/LineItem/{productId} should fail without If-Match")
    void testRemoveLineItem_NoIfMatchHeader() {
        // Arrange
        when(httpHeaders.getRequestHeader("If-Match")).thenReturn(null);

        // Act
        Response response = customerOrderResource.removeLineItem(TEST_PRODUCT_ID, httpHeaders);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.PRECONDITION_FAILED.getStatusCode());
        verifyNoInteractions(customerOrderServices);
    }

    @Test
    @DisplayName("DELETE /Customer/OpenOrder/LineItem/{productId} should throw 404 when customer not found")
    void testRemoveLineItem_CustomerNotFound() throws Exception {
        // Arrange
        List<String> matchHeaders = Arrays.asList(String.valueOf(TEST_VERSION));
        when(httpHeaders.getRequestHeader("If-Match")).thenReturn(matchHeaders);
        when(customerOrderServices.removeLineItem(anyInt(), anyLong()))
                .thenThrow(new CustomerDoesNotExistException());

        // Act & Assert
        assertThatThrownBy(() -> customerOrderResource.removeLineItem(TEST_PRODUCT_ID, httpHeaders))
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("DELETE /Customer/OpenOrder/LineItem/{productId} should throw 412 for version mismatch")
    void testRemoveLineItem_OrderModified() throws Exception {
        // Arrange
        List<String> matchHeaders = Arrays.asList(String.valueOf(TEST_VERSION));
        when(httpHeaders.getRequestHeader("If-Match")).thenReturn(matchHeaders);
        when(customerOrderServices.removeLineItem(anyInt(), anyLong()))
                .thenThrow(new OrderModifiedException());

        // Act & Assert
        assertThatThrownBy(() -> customerOrderResource.removeLineItem(TEST_PRODUCT_ID, httpHeaders))
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(Response.Status.PRECONDITION_FAILED.getStatusCode());
    }

    // ===== submitOrder Tests =====

    @Test
    @DisplayName("POST /Customer/OpenOrder should submit order with If-Match header")
    void testSubmitOrder_Success() throws Exception {
        // Arrange
        List<String> matchHeaders = Arrays.asList(String.valueOf(TEST_VERSION));
        when(httpHeaders.getRequestHeader("If-Match")).thenReturn(matchHeaders);
        doNothing().when(customerOrderServices).submit(TEST_VERSION);

        // Act
        Response response = customerOrderResource.submitOrder(httpHeaders);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(customerOrderServices).submit(TEST_VERSION);
    }

    @Test
    @DisplayName("POST /Customer/OpenOrder should fail without If-Match header")
    void testSubmitOrder_NoIfMatchHeader() {
        // Arrange
        when(httpHeaders.getRequestHeader("If-Match")).thenReturn(null);

        // Act
        Response response = customerOrderResource.submitOrder(httpHeaders);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.PRECONDITION_FAILED.getStatusCode());
        verifyNoInteractions(customerOrderServices);
    }

    @Test
    @DisplayName("POST /Customer/OpenOrder should throw 404 when customer not found")
    void testSubmitOrder_CustomerNotFound() throws Exception {
        // Arrange
        List<String> matchHeaders = Arrays.asList(String.valueOf(TEST_VERSION));
        when(httpHeaders.getRequestHeader("If-Match")).thenReturn(matchHeaders);
        doThrow(new CustomerDoesNotExistException()).when(customerOrderServices).submit(anyLong());

        // Act & Assert
        assertThatThrownBy(() -> customerOrderResource.submitOrder(httpHeaders))
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("POST /Customer/OpenOrder should throw 412 for version mismatch")
    void testSubmitOrder_OrderModified() throws Exception {
        // Arrange
        List<String> matchHeaders = Arrays.asList(String.valueOf(TEST_VERSION));
        when(httpHeaders.getRequestHeader("If-Match")).thenReturn(matchHeaders);
        doThrow(new OrderModifiedException()).when(customerOrderServices).submit(anyLong());

        // Act & Assert
        assertThatThrownBy(() -> customerOrderResource.submitOrder(httpHeaders))
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(Response.Status.PRECONDITION_FAILED.getStatusCode());
    }

    // ===== getOrderHistory Tests =====

    @Test
    @DisplayName("GET /Customer/Orders should return order history")
    void testGetOrderHistory_Success() throws Exception {
        // Arrange
        Set<Order> orders = new HashSet<>();
        orders.add(testOrder);
        
        Date lastModified = new Date();
        when(customerOrderServices.getOrderHistoryLastUpdatedTime()).thenReturn(lastModified);
        when(customerOrderServices.loadCustomerHistory()).thenReturn(orders);
        when(httpHeaders.getRequestHeader("If-Modified-Since")).thenReturn(null);

        // Act
        Response response = customerOrderResource.getOrderHistory(httpHeaders);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(Set.class);
        assertThat(response.getMetadata().getFirst("Last-Modified")).isEqualTo(lastModified);
        
        verify(customerOrderServices).loadCustomerHistory();
    }

    @Test
    @DisplayName("GET /Customer/Orders should return 304 Not Modified when not changed")
    void testGetOrderHistory_NotModified() throws Exception {
        // Arrange
        Date lastModified = new Date(System.currentTimeMillis() - 10000); // 10 seconds ago
        Date headerDate = new Date(); // Now
        
        List<String> modifiedSinceHeaders = Arrays.asList("2025-11-05 14:00:00.000");
        when(httpHeaders.getRequestHeader("If-Modified-Since")).thenReturn(modifiedSinceHeaders);
        when(customerOrderServices.getOrderHistoryLastUpdatedTime()).thenReturn(lastModified);

        // Act
        Response response = customerOrderResource.getOrderHistory(httpHeaders);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_MODIFIED.getStatusCode());
        verify(customerOrderServices, never()).loadCustomerHistory();
    }

    @Test
    @DisplayName("GET /Customer/Orders should throw 404 when customer not found")
    void testGetOrderHistory_CustomerNotFound() throws Exception {
        // Arrange
        when(httpHeaders.getRequestHeader("If-Modified-Since")).thenReturn(null);
        when(customerOrderServices.getOrderHistoryLastUpdatedTime()).thenReturn(new Date());
        when(customerOrderServices.loadCustomerHistory())
                .thenThrow(new CustomerDoesNotExistException());

        // Act & Assert
        assertThatThrownBy(() -> customerOrderResource.getOrderHistory(httpHeaders))
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    // ===== updateInfo Tests =====

    @Test
    @DisplayName("POST /Customer/Info should update customer info")
    void testUpdateInfo_Success() throws Exception {
        // Arrange
        HashMap<String, Object> info = new HashMap<>();
        info.put("type", "RESIDENTIAL");
        info.put("householdSize", 4);
        
        doNothing().when(customerOrderServices).updateInfo(any());

        // Act
        Response response = customerOrderResource.updateInfo(info);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(customerOrderServices).updateInfo(info);
    }

    @Test
    @DisplayName("POST /Customer/Info should throw 404 when customer not found")
    void testUpdateInfo_CustomerNotFound() throws Exception {
        // Arrange
        HashMap<String, Object> info = new HashMap<>();
        doThrow(new CustomerDoesNotExistException()).when(customerOrderServices).updateInfo(any());

        // Act & Assert
        assertThatThrownBy(() -> customerOrderResource.updateInfo(info))
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("POST /Customer/Info should handle general exceptions")
    void testUpdateInfo_GeneralException() throws Exception {
        // Arrange
        HashMap<String, Object> info = new HashMap<>();
        doThrow(new RuntimeException()).when(customerOrderServices).updateInfo(any());

        // Act & Assert
        assertThatThrownBy(() -> customerOrderResource.updateInfo(info))
                .isInstanceOf(WebApplicationException.class);
    }
}

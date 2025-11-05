package org.pwte.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pwte.example.domain.*;
import org.pwte.example.exception.*;

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for CustomerOrderServicesImpl.
 * Uses Mockito to mock EntityManager and SessionContext dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerOrderServicesImpl Tests")
class CustomerOrderServicesImplTest {

    @Mock
    private EntityManager em;

    @Mock
    private SessionContext ctx;

    @Mock
    private Query query;

    @Mock
    private Principal principal;

    @InjectMocks
    private CustomerOrderServicesImpl service;

    private static final String TEST_USER = "testuser";
    private static final int TEST_CUSTOMER_ID = 100;
    private static final int TEST_PRODUCT_ID = 200;
    private static final int TEST_ORDER_ID = 300;

    private ResidentialCustomer testCustomer;
    private Product testProduct;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Setup test customer
        testCustomer = new ResidentialCustomer();
        testCustomer.setCustomerId(TEST_CUSTOMER_ID);
        testCustomer.setUser(TEST_USER);
        testCustomer.setFirstName("Test");
        testCustomer.setLastName("User");

        // Setup test product
        testProduct = new Product();
        testProduct.setProductId(TEST_PRODUCT_ID);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));

        // Setup test order
        testOrder = new Order();
        testOrder.setOrderId(TEST_ORDER_ID);
        testOrder.setCustomer(testCustomer);
        testOrder.setStatus(Order.Status.OPEN);
        testOrder.setTotal(new BigDecimal("0.00"));
        testOrder.setVersion(1L);
        testOrder.setLineitems(new HashSet<>());

        // Default mock behaviors
        when(ctx.getCallerPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn(TEST_USER);
    }

    // ===== loadCustomer Tests =====

    @Test
    @DisplayName("loadCustomer should return customer when user exists")
    void testLoadCustomer_Success() throws Exception {
        // Arrange
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act
        AbstractCustomer result = service.loadCustomer();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo(TEST_CUSTOMER_ID);
        assertThat(result.getUser()).isEqualTo(TEST_USER);
        verify(em).createQuery("select c from AbstractCustomer c where c.user = :user");
        verify(query).setParameter("user", TEST_USER);
    }

    @Test
    @DisplayName("loadCustomer should throw exception when no result found")
    void testLoadCustomer_NotFound() {
        // Arrange
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new javax.persistence.NoResultException());

        // Act & Assert
        assertThatThrownBy(() -> service.loadCustomer())
                .isInstanceOf(javax.persistence.NoResultException.class);
    }

    // ===== openOrder Tests =====

    @Test
    @DisplayName("openOrder should create new order when no open order exists")
    void testOpenOrder_Success() throws Exception {
        // Arrange
        testCustomer.setOpenOrder(null);
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act
        Order result = service.openOrder();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Order.Status.OPEN);
        assertThat(result.getCustomer()).isEqualTo(testCustomer);
        assertThat(result.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(em).persist(any(Order.class));
    }

    @Test
    @DisplayName("openOrder should throw exception when order already open")
    void testOpenOrder_AlreadyOpen() throws Exception {
        // Arrange
        testCustomer.setOpenOrder(testOrder);
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act & Assert
        assertThatThrownBy(() -> service.openOrder())
                .isInstanceOf(OrderAlreadyOpenException.class);
        verify(em, never()).persist(any());
    }

    // ===== addLineItem Tests =====

    @Test
    @DisplayName("addLineItem should add new line item when product exists")
    void testAddLineItem_NewItem_Success() throws Exception {
        // Arrange
        LineItem newLineItem = new LineItem();
        newLineItem.setProductId(TEST_PRODUCT_ID);
        newLineItem.setQuantity(5L);
        newLineItem.setVersion(1L);

        testCustomer.setOpenOrder(testOrder);

        when(em.find(Product.class, TEST_PRODUCT_ID)).thenReturn(testProduct);
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act
        Order result = service.addLineItem(newLineItem);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getLineitems()).hasSize(1);
        
        LineItem addedItem = result.getLineitems().iterator().next();
        assertThat(addedItem.getProductId()).isEqualTo(TEST_PRODUCT_ID);
        assertThat(addedItem.getQuantity()).isEqualTo(5L);
        assertThat(addedItem.getAmount()).isEqualByComparingTo(new BigDecimal("499.95")); // 99.99 * 5
        
        verify(em).persist(any(LineItem.class));
    }

    @Test
    @DisplayName("addLineItem should update quantity when product already in order")
    void testAddLineItem_UpdateExisting_Success() throws Exception {
        // Arrange
        LineItem existingLineItem = new LineItem();
        existingLineItem.setProductId(TEST_PRODUCT_ID);
        existingLineItem.setQuantity(3L);
        existingLineItem.setAmount(new BigDecimal("299.97"));
        
        Set<LineItem> lineItems = new HashSet<>();
        lineItems.add(existingLineItem);
        testOrder.setLineitems(lineItems);
        testCustomer.setOpenOrder(testOrder);

        LineItem newLineItem = new LineItem();
        newLineItem.setProductId(TEST_PRODUCT_ID);
        newLineItem.setQuantity(2L);
        newLineItem.setVersion(1L);

        when(em.find(Product.class, TEST_PRODUCT_ID)).thenReturn(testProduct);
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act
        Order result = service.addLineItem(newLineItem);

        // Assert
        assertThat(result.getLineitems()).hasSize(1);
        LineItem updatedItem = result.getLineitems().iterator().next();
        assertThat(updatedItem.getQuantity()).isEqualTo(5L); // 3 + 2
        assertThat(updatedItem.getAmount()).isEqualByComparingTo(new BigDecimal("499.95")); // 299.97 + 199.98
        
        verify(em, never()).persist(any(LineItem.class)); // No new persist, just update
    }

    @Test
    @DisplayName("addLineItem should throw exception when product does not exist")
    void testAddLineItem_ProductNotFound() {
        // Arrange
        LineItem newLineItem = new LineItem();
        newLineItem.setProductId(TEST_PRODUCT_ID);
        newLineItem.setQuantity(1L);

        when(em.find(Product.class, TEST_PRODUCT_ID)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> service.addLineItem(newLineItem))
                .isInstanceOf(ProductDoesNotExistException.class);
        verify(em, never()).persist(any());
    }

    @Test
    @DisplayName("addLineItem should throw exception when quantity is zero")
    void testAddLineItem_InvalidQuantityZero() {
        // Arrange
        LineItem newLineItem = new LineItem();
        newLineItem.setProductId(TEST_PRODUCT_ID);
        newLineItem.setQuantity(0L);

        when(em.find(Product.class, TEST_PRODUCT_ID)).thenReturn(testProduct);

        // Act & Assert
        assertThatThrownBy(() -> service.addLineItem(newLineItem))
                .isInstanceOf(InvalidQuantityException.class);
    }

    @Test
    @DisplayName("addLineItem should throw exception when quantity is negative")
    void testAddLineItem_InvalidQuantityNegative() {
        // Arrange
        LineItem newLineItem = new LineItem();
        newLineItem.setProductId(TEST_PRODUCT_ID);
        newLineItem.setQuantity(-5L);

        when(em.find(Product.class, TEST_PRODUCT_ID)).thenReturn(testProduct);

        // Act & Assert
        assertThatThrownBy(() -> service.addLineItem(newLineItem))
                .isInstanceOf(InvalidQuantityException.class);
    }

    @Test
    @DisplayName("addLineItem should throw exception when order version mismatch")
    void testAddLineItem_VersionMismatch() throws Exception {
        // Arrange
        testOrder.setVersion(5L);
        testCustomer.setOpenOrder(testOrder);

        LineItem newLineItem = new LineItem();
        newLineItem.setProductId(TEST_PRODUCT_ID);
        newLineItem.setQuantity(1L);
        newLineItem.setVersion(3L); // Different version

        when(em.find(Product.class, TEST_PRODUCT_ID)).thenReturn(testProduct);
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act & Assert
        assertThatThrownBy(() -> service.addLineItem(newLineItem))
                .isInstanceOf(OrderModifiedException.class);
    }

    @Test
    @DisplayName("addLineItem should create order if no open order exists")
    void testAddLineItem_CreatesOrderIfNone() throws Exception {
        // Arrange
        testCustomer.setOpenOrder(null);

        LineItem newLineItem = new LineItem();
        newLineItem.setProductId(TEST_PRODUCT_ID);
        newLineItem.setQuantity(2L);
        newLineItem.setVersion(0L);

        when(em.find(Product.class, TEST_PRODUCT_ID)).thenReturn(testProduct);
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act
        Order result = service.addLineItem(newLineItem);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Order.Status.OPEN);
        verify(em).persist(any(Order.class)); // Order created
        verify(em).persist(any(LineItem.class)); // Line item added
    }

    // ===== removeLineItem Tests =====

    @Test
    @DisplayName("removeLineItem should remove line item when it exists")
    void testRemoveLineItem_Success() throws Exception {
        // Arrange
        LineItem existingLineItem = new LineItem();
        existingLineItem.setProductId(TEST_PRODUCT_ID);
        existingLineItem.setQuantity(2L);
        
        Set<LineItem> lineItems = new HashSet<>();
        lineItems.add(existingLineItem);
        testOrder.setLineitems(lineItems);
        testCustomer.setOpenOrder(testOrder);

        when(em.find(Product.class, TEST_PRODUCT_ID)).thenReturn(testProduct);
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act
        Order result = service.removeLineItem(TEST_PRODUCT_ID, 1L);

        // Assert
        assertThat(result.getLineitems()).isEmpty();
        verify(em).remove(existingLineItem);
    }

    @Test
    @DisplayName("removeLineItem should throw exception when product does not exist")
    void testRemoveLineItem_ProductNotFound() {
        // Arrange
        when(em.find(Product.class, TEST_PRODUCT_ID)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> service.removeLineItem(TEST_PRODUCT_ID, 1L))
                .isInstanceOf(ProductDoesNotExistException.class);
    }

    @Test
    @DisplayName("removeLineItem should throw exception when no open order")
    void testRemoveLineItem_NoOpenOrder() throws Exception {
        // Arrange
        testCustomer.setOpenOrder(null);

        when(em.find(Product.class, TEST_PRODUCT_ID)).thenReturn(testProduct);
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act & Assert
        assertThatThrownBy(() -> service.removeLineItem(TEST_PRODUCT_ID, 1L))
                .isInstanceOf(OrderNotOpenException.class);
    }

    @Test
    @DisplayName("removeLineItem should throw exception when order version mismatch")
    void testRemoveLineItem_VersionMismatch() throws Exception {
        // Arrange
        testOrder.setVersion(5L);
        testCustomer.setOpenOrder(testOrder);

        when(em.find(Product.class, TEST_PRODUCT_ID)).thenReturn(testProduct);
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act & Assert
        assertThatThrownBy(() -> service.removeLineItem(TEST_PRODUCT_ID, 3L))
                .isInstanceOf(OrderModifiedException.class);
    }

    @Test
    @DisplayName("removeLineItem should throw exception when line item not found")
    void testRemoveLineItem_LineItemNotFound() throws Exception {
        // Arrange
        testOrder.setLineitems(new HashSet<>()); // Empty line items
        testCustomer.setOpenOrder(testOrder);

        when(em.find(Product.class, TEST_PRODUCT_ID)).thenReturn(testProduct);
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act & Assert
        assertThatThrownBy(() -> service.removeLineItem(TEST_PRODUCT_ID, 1L))
                .isInstanceOf(NoLineItemsException.class);
    }

    // ===== submit Tests =====

    @Test
    @DisplayName("submit should submit order when valid")
    void testSubmit_Success() throws Exception {
        // Arrange
        LineItem lineItem = new LineItem();
        lineItem.setProductId(TEST_PRODUCT_ID);
        lineItem.setQuantity(1L);
        
        Set<LineItem> lineItems = new HashSet<>();
        lineItems.add(lineItem);
        testOrder.setLineitems(lineItems);
        testCustomer.setOpenOrder(testOrder);

        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act
        service.submit(1L);

        // Assert
        assertThat(testOrder.getStatus()).isEqualTo(Order.Status.SUBMITTED);
        assertThat(testOrder.getSubmittedTime()).isNotNull();
        assertThat(testCustomer.getOpenOrder()).isNull();
    }

    @Test
    @DisplayName("submit should throw exception when no open order")
    void testSubmit_NoOpenOrder() throws Exception {
        // Arrange
        testCustomer.setOpenOrder(null);

        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act & Assert
        assertThatThrownBy(() -> service.submit(1L))
                .isInstanceOf(OrderNotOpenException.class);
    }

    @Test
    @DisplayName("submit should throw exception when order not in OPEN status")
    void testSubmit_OrderNotOpen() throws Exception {
        // Arrange
        testOrder.setStatus(Order.Status.SUBMITTED);
        testCustomer.setOpenOrder(testOrder);

        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act & Assert
        assertThatThrownBy(() -> service.submit(1L))
                .isInstanceOf(OrderNotOpenException.class);
    }

    @Test
    @DisplayName("submit should throw exception when no line items")
    void testSubmit_NoLineItems() throws Exception {
        // Arrange
        testOrder.setLineitems(new HashSet<>());
        testCustomer.setOpenOrder(testOrder);

        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act & Assert
        assertThatThrownBy(() -> service.submit(1L))
                .isInstanceOf(NoLineItemsException.class);
    }

    @Test
    @DisplayName("submit should throw exception when version mismatch")
    void testSubmit_VersionMismatch() throws Exception {
        // Arrange
        testOrder.setVersion(5L);
        LineItem lineItem = new LineItem();
        Set<LineItem> lineItems = new HashSet<>();
        lineItems.add(lineItem);
        testOrder.setLineitems(lineItems);
        testCustomer.setOpenOrder(testOrder);

        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act & Assert
        assertThatThrownBy(() -> service.submit(3L))
                .isInstanceOf(OrderModifiedException.class);
    }

    // ===== loadCustomerHistory Tests =====

    @Test
    @DisplayName("loadCustomerHistory should return customer orders")
    void testLoadCustomerHistory_Success() throws Exception {
        // Arrange
        Set<Order> orders = new HashSet<>();
        orders.add(testOrder);
        testCustomer.setOrders(orders);

        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act
        Set<Order> result = service.loadCustomerHistory();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result).contains(testOrder);
    }

    @Test
    @DisplayName("loadCustomerHistory should return empty set when no orders")
    void testLoadCustomerHistory_NoOrders() throws Exception {
        // Arrange
        testCustomer.setOrders(new HashSet<>());

        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act
        Set<Order> result = service.loadCustomerHistory();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    // ===== getOrderHistoryLastUpdatedTime Tests =====

    @Test
    @DisplayName("getOrderHistoryLastUpdatedTime should return max submitted time")
    void testGetOrderHistoryLastUpdatedTime_Success() {
        // Arrange
        Date expectedDate = new Date();
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(expectedDate);

        // Act
        Date result = service.getOrderHistoryLastUpdatedTime();

        // Assert
        assertThat(result).isEqualTo(expectedDate);
        verify(query).setParameter("user", TEST_USER);
    }

    @Test
    @DisplayName("getOrderHistoryLastUpdatedTime should return null when no orders")
    void testGetOrderHistoryLastUpdatedTime_NoOrders() {
        // Arrange
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(null);

        // Act
        Date result = service.getOrderHistoryLastUpdatedTime();

        // Assert
        assertThat(result).isNull();
    }

    // ===== updateAddress Tests =====

    @Test
    @DisplayName("updateAddress should update customer address")
    void testUpdateAddress_Success() throws Exception {
        // Arrange
        Address newAddress = new Address();
        newAddress.setStreet("123 New St");
        newAddress.setCity("New City");
        newAddress.setState("NC");
        newAddress.setZip("12345");

        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act
        service.updateAddress(newAddress);

        // Assert
        assertThat(testCustomer.getAddress()).isEqualTo(newAddress);
        assertThat(testCustomer.getAddress().getStreet()).isEqualTo("123 New St");
    }

    @Test
    @DisplayName("updateAddress should handle null address")
    void testUpdateAddress_Null() throws Exception {
        // Arrange
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act
        service.updateAddress(null);

        // Assert
        assertThat(testCustomer.getAddress()).isNull();
    }

    // ===== updateInfo Tests =====

    @Test
    @DisplayName("updateInfo should update business customer description")
    void testUpdateInfo_BusinessCustomer() throws Exception {
        // Arrange
        BusinessCustomer businessCustomer = new BusinessCustomer();
        businessCustomer.setCustomerId(TEST_CUSTOMER_ID);
        businessCustomer.setUser(TEST_USER);

        HashMap<String, Object> info = new HashMap<>();
        info.put("type", "BUSINESS");
        info.put("description", "Updated Business Description");

        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(businessCustomer);

        // Act
        service.updateInfo(info);

        // Assert
        assertThat(businessCustomer.getDescription()).isEqualTo("Updated Business Description");
    }

    @Test
    @DisplayName("updateInfo should update residential customer household size")
    void testUpdateInfo_ResidentialCustomer() throws Exception {
        // Arrange
        HashMap<String, Object> info = new HashMap<>();
        info.put("type", "RESIDENTIAL");
        info.put("householdSize", 4);

        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act
        service.updateInfo(info);

        // Assert
        assertThat(testCustomer.getHouseholdSize()).isEqualTo((short) 4);
    }

    @Test
    @DisplayName("updateInfo should handle different household sizes")
    void testUpdateInfo_DifferentHouseholdSizes() throws Exception {
        // Arrange
        HashMap<String, Object> info = new HashMap<>();
        info.put("type", "RESIDENTIAL");
        info.put("householdSize", 10);

        when(em.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("user"), eq(TEST_USER))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testCustomer);

        // Act
        service.updateInfo(info);

        // Assert
        assertThat(testCustomer.getHouseholdSize()).isEqualTo((short) 10);
    }
}

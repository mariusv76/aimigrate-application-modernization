package org.pwte.example.api;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pwte.example.domain.BusinessCustomer;
import org.pwte.example.domain.AbstractCustomer;
import org.pwte.example.domain.Address;
import org.pwte.example.domain.LineItem;
import org.pwte.example.domain.Order;
import org.pwte.example.exception.CustomerDoesNotExistException;
import org.pwte.example.exception.GeneralPersistenceException;
import org.pwte.example.exception.InvalidQuantityException;
import org.pwte.example.exception.NoLineItemsException;
import org.pwte.example.exception.OrderModifiedException;
import org.pwte.example.exception.OrderNotOpenException;
import org.pwte.example.exception.ProductDoesNotExistException;

class CustomerResourceTest {

    @Test
    @DisplayName("getBusinessCustomers returns stubbed list")
    void testGetBusinessCustomers() throws Exception {
        CustomerResource resource = new CustomerResource();

        BusinessCustomer bc = new BusinessCustomer();
        bc.setCustomerId(99);
        bc.setName("Stub Business");
        bc.setUser("stubuser");
        bc.setDescription("Stub description");
        bc.setVolumeDiscount("Y");
        bc.setBusinessPartner("N");

        List<BusinessCustomer> list = List.of(bc);

        org.pwte.example.service.CustomerOrderServices stub = new org.pwte.example.service.CustomerOrderServices() {
            @Override
            public AbstractCustomer loadCustomer() throws CustomerDoesNotExistException, GeneralPersistenceException {
                return bc;
            }
            @Override
            public Order addLineItem(LineItem lineItem) throws CustomerDoesNotExistException, OrderNotOpenException,
                    ProductDoesNotExistException, GeneralPersistenceException, InvalidQuantityException, OrderModifiedException {
                throw new UnsupportedOperationException();
            }
            @Override
            public Order removeLineItem(int productId, long version) throws CustomerDoesNotExistException,
                    OrderNotOpenException, ProductDoesNotExistException, NoLineItemsException, GeneralPersistenceException,
                    OrderModifiedException {
                return null;
            }
            @Override
            public void submit(long version) throws CustomerDoesNotExistException, OrderNotOpenException,
                    NoLineItemsException, GeneralPersistenceException, OrderModifiedException {
                // no-op
            }
            @Override
            public Set<Order> loadCustomerHistory() throws CustomerDoesNotExistException, GeneralPersistenceException {
                return Set.of();
            }
            @Override
            public void updateAddress(Address address) throws CustomerDoesNotExistException, GeneralPersistenceException {
                // no-op
            }
            @Override
            public Date getOrderHistoryLastUpdatedTime() {
                return new Date();
            }
            @Override
            public void updateInfo(HashMap<String, Object> info) throws GeneralPersistenceException, CustomerDoesNotExistException {
                // no-op
            }
            @Override
            public List<BusinessCustomer> listBusinessCustomers() {
                return list;
            }
        };

        var field = CustomerResource.class.getDeclaredField("services");
        field.setAccessible(true);
        field.set(resource, stub);

        var response = resource.getBusinessCustomers();
        assertEquals(200, response.getStatus());
        @SuppressWarnings("unchecked")
        List<BusinessCustomer> result = (List<BusinessCustomer>) response.getEntity();
        assertEquals(1, result.size());
        assertEquals(99, result.get(0).getCustomerId());
        assertEquals("Stub Business", result.get(0).getName());
        assertTrue(result.get(0).isVolumeDiscount());
        assertFalse(result.get(0).isBusinessPartner());
    }
}

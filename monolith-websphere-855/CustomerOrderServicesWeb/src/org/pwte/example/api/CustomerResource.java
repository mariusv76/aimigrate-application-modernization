package org.pwte.example.api;

import java.util.List;
import java.util.stream.Collectors;

import org.pwte.example.api.dto.BusinessCustomerDTO;
import org.pwte.example.domain.BusinessCustomer;

import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @EJB
    private org.pwte.example.service.CustomerOrderServices services;

    @GET
    @Path("/business")
    public Response getBusinessCustomers() {
        List<BusinessCustomer> entities = services.listBusinessCustomers();
        
        // Project to DTO to avoid JPA entity serialization issues
        List<BusinessCustomerDTO> dtoList = entities.stream()
            .map(e -> new BusinessCustomerDTO(
                e.getCustomerId(),
                e.getName(),
                e.getUser(),
                e.getDescription(),
                e.isVolumeDiscount(),
                e.isBusinessPartner()
            ))
            .collect(Collectors.toList());
        
        return Response.ok(dtoList).build();
    }
}

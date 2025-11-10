package org.pwte.example.api;

import java.util.List;

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
        try {
            List<BusinessCustomer> entities = services.listBusinessCustomers();
            return Response.ok(entities).build();
        } catch (Exception e) {
            return Response.serverError().entity("Error: " + e.getMessage()).build();
        }
    }
}

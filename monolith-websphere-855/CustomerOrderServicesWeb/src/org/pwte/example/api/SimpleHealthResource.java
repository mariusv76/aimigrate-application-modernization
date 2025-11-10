package org.pwte.example.api;

import jakarta.annotation.Resource;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import javax.sql.DataSource;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class SimpleHealthResource {

    @Resource(lookup = "jdbc/orderds")
    private DataSource ds;

    @GET
    @Path("/simple")
    public Response simple() {
        try {
            ds.getConnection().close();
            return Response.ok("{\"status\":\"UP\"}").build();
        } catch (Exception e) {
            return Response.serverError().entity("{\"status\":\"DOWN\",\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }
}
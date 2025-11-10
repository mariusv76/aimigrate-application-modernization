package org.pwte.example.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/ping")
@Produces(MediaType.APPLICATION_JSON)
public class PingResource {

    @GET
    public Response ping() {
        return Response.ok("{\"status\":\"ok\"}").type(MediaType.APPLICATION_JSON).build();
    }
}

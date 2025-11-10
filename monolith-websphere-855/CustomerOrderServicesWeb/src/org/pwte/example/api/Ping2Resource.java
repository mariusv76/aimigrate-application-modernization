package org.pwte.example.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/ping2")
@Produces(MediaType.TEXT_PLAIN)
public class Ping2Resource {

    @GET
    public Response ping() {
        return Response.ok("pong2").build();
    }
}

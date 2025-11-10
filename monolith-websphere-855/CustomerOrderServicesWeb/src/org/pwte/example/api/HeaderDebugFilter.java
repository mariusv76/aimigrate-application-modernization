package org.pwte.example.api;

import java.util.List;
import java.util.Map;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class HeaderDebugFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        // Log all response headers prior to commit to identify any with illegal characters
        for (Map.Entry<String, List<Object>> entry : responseContext.getHeaders().entrySet()) {
            System.out.println("DBG-HEADER " + entry.getKey() + "=" + entry.getValue());
        }
    }
}

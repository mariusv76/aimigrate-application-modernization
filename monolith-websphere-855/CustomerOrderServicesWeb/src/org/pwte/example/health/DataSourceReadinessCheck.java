package org.pwte.example.health;

import java.sql.Connection;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Readiness
public class DataSourceReadinessCheck implements HealthCheck {

    @Resource(name = "jdbc/orderds")
    private DataSource ds;

    private static final Logger LOG = Logger.getLogger(DataSourceReadinessCheck.class.getName());

    @Override
    public HealthCheckResponse call() {
        if (ds == null) {
            LOG.warning("Injection null for jdbc/orderds; attempting manual JNDI lookup.");
            try {
                InitialContext ctx = new InitialContext();
                ds = (DataSource) ctx.lookup("jdbc/orderds");
            } catch (NamingException ne) {
                LOG.severe("JNDI lookup failed: " + ne.getMessage());
                return HealthCheckResponse.named("datasource-orderds").down()
                        .withData("error", "injection-null")
                        .withData("jndi", safeMsg(ne.getMessage()))
                        .build();
            }
        }
        try (Connection c = ds.getConnection()) {
            return HealthCheckResponse.named("datasource-orderds").up().build();
        } catch (Exception e) {
            LOG.severe("DataSource readiness failure: " + e.getClass().getName() + ": " + e.getMessage());
            return HealthCheckResponse.named("datasource-orderds").down()
                    .withData("exception", e.getClass().getSimpleName())
                    .withData("message", safeMsg(e.getMessage()))
                    .build();
        }
    }

    private String safeMsg(String msg) {
        if (msg == null) return "";
        // Limit size to avoid overly large health payloads
        int max = 200;
        return msg.length() <= max ? msg : msg.substring(0, max);
    }
}

package org.pwte.example.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import jakarta.annotation.Resource;
import javax.sql.DataSource;

@Readiness
public class DataSourceReadinessCheck implements HealthCheck {

    @Resource(lookup = "jdbc/orderds")
    private DataSource ds;

    @Override
    public HealthCheckResponse call() {
        try {
            ds.getConnection().close();
            return HealthCheckResponse.up("datasource-orderds");
        } catch (Exception e) {
            return HealthCheckResponse.down("datasource-orderds");
        }
    }
}

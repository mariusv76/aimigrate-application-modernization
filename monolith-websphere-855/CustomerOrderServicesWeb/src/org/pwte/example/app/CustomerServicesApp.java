package org.pwte.example.app;

// Legacy bootstrap adjusted: now a minimal valid Jakarta Application subclass
// WITHOUT @ApplicationPath so the annotated RestApplication remains authoritative.
// Prevents ClassCastException during Resteasy initialization.
public class CustomerServicesApp extends jakarta.ws.rs.core.Application {
    // No providers or singletons registered here.
}

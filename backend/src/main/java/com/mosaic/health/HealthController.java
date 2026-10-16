package com.mosaic.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health and readiness endpoints for Mosaic.
 *
 * Used by container orchestrators (e.g. Kubernetes, Docker Compose, AWS ECS)
 * for liveness and readiness probes, and by load balancers for routing decisions.
 */
@RestController
@Tag(name = "Health", description = "Liveness and readiness check endpoints")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    @Value("${mosaic.version:0.1.0}")
    private String version;

    private final DataSource dataSource;

    @Autowired
    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Liveness probe: returns 200 if the application process is running.
     */
    @GetMapping("/health")
    @Operation(summary = "Liveness check", description = "Returns UP if the application is running")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("service", "mosaic-backend");
        body.put("version", version);
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(body);
    }

    /**
     * Readiness probe: checks if dependent services (database) are reachable.
     */
    @GetMapping("/ready")
    @Operation(summary = "Readiness check", description = "Verifies database connectivity before accepting traffic")
    public ResponseEntity<Map<String, Object>> ready() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service", "mosaic-backend");
        body.put("timestamp", Instant.now().toString());

        try (Connection connection = dataSource.getConnection()) {
            boolean isDbValid = connection.isValid(2); // 2 second timeout
            if (isDbValid) {
                body.put("status", "UP");
                body.put("database", "CONNECTED");
                return ResponseEntity.ok(body);
            } else {
                body.put("status", "DOWN");
                body.put("database", "CONNECTION_INVALID");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
            }
        } catch (SQLException e) {
            log.warn("Database readiness check failed: {}", e.getMessage());
            body.put("status", "DOWN");
            body.put("database", "DISCONNECTED");
            body.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
        }
    }
}

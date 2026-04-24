package com.expensetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class ExpenseTrackerApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ExpenseTrackerApplication.class);

        // Render Postgres provides DATABASE_URL like: postgresql://user:password@host:5432/dbname
        // Spring expects a JDBC URL (jdbc:postgresql://...) plus optional username/password.
        // We derive the Spring properties only if they weren't provided explicitly.
        Map<String, Object> defaults = new HashMap<>();
        maybeApplyRenderDatabaseUrl(defaults);
        app.setDefaultProperties(defaults);

        app.run(args);
    }

    private static void maybeApplyRenderDatabaseUrl(Map<String, Object> defaults) {
        // If user provided datasource config explicitly, do not override.
        if (System.getenv("SPRING_DATASOURCE_URL") != null) return;
        if (System.getenv("SPRING_DATASOURCE_USERNAME") != null) return;
        if (System.getenv("SPRING_DATASOURCE_PASSWORD") != null) return;
        if (System.getProperty("spring.datasource.url") != null) return;

        String raw = System.getenv("DATABASE_URL");
        if (raw == null || raw.isBlank()) return;

        // Render uses "postgresql://..." in connection strings.
        // java.net.URI also understands "postgres://..." so we support both.
        if (!(raw.startsWith("postgresql://") || raw.startsWith("postgres://"))) return;

        URI uri = URI.create(raw);
        String host = uri.getHost();
        int port = (uri.getPort() == -1) ? 5432 : uri.getPort();
        String db = uri.getPath();
        if (db != null && db.startsWith("/")) db = db.substring(1);

        if (host == null || host.isBlank() || db == null || db.isBlank()) return;

        String jdbc = "jdbc:postgresql://" + host + ":" + port + "/" + db;
        if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
            jdbc = jdbc + "?" + uri.getQuery();
        }
        defaults.put("spring.datasource.url", jdbc);

        String userInfo = uri.getUserInfo();
        if (userInfo != null && !userInfo.isBlank()) {
            String[] parts = userInfo.split(":", 2);
            String user = urlDecode(parts[0]);
            defaults.put("spring.datasource.username", user);
            if (parts.length == 2) {
                defaults.put("spring.datasource.password", urlDecode(parts[1]));
            }
        }
    }

    private static String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}

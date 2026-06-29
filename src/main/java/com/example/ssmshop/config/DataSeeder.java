package com.example.ssmshop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Runs schema.sql + data.sql exactly once per fresh database, gated by
 * the app_meta.schema_initialized flag. Subsequent restarts skip the work
 * entirely, which is what makes cold starts fast.
 */
@Component
public class DataSeeder {
    private static final Logger LOG = LoggerFactory.getLogger(DataSeeder.class);
    private static final String MIGRATION_VERSION = "20260629-fulfillment-b";

    private final DataSource dataSource;

    public DataSeeder(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        try (Connection connection = dataSource.getConnection()) {
            ensureMetaTable(connection);
            if (alreadyInitialized(connection)) {
                runMigrationsIfNeeded(connection);
                LOG.info("Database already initialized, startup migration check completed.");
                return;
            }
            ensureSchema(connection);
            LOG.info("Seeding demo data on first run...");
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("db/data.sql"));
            upsertMeta(connection, "schema_initialized", "true");
            upsertMeta(connection, "schema_migration_version", MIGRATION_VERSION);
            LOG.info("Demo data seed completed.");
        } catch (SQLException ex) {
            LOG.error("Failed to seed database: {}", ex.getMessage(), ex);
        }
    }

    private void ensureMetaTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS app_meta (
                        meta_key VARCHAR(80) PRIMARY KEY,
                        meta_value VARCHAR(255),
                        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                    )
                    """);
        }
    }

    private void ensureSchema(Connection connection) {
        ScriptUtils.executeSqlScript(connection, new ClassPathResource("db/schema.sql"));
    }

    private void runMigrationsIfNeeded(Connection connection) throws SQLException {
        String version = metaValue(connection, "schema_migration_version");
        if (MIGRATION_VERSION.equals(version)) {
            LOG.info("Schema migrations already up to date: {}", MIGRATION_VERSION);
            return;
        }
        runMigrations(connection);
        upsertMeta(connection, "schema_migration_version", MIGRATION_VERSION);
    }

    /**
     * Idempotent fix-ups for databases that were created with older
     * versions of the schema. Each statement is best-effort: if it fails
     * the migration is logged and skipped so a fresh DB still starts cleanly.
     */
    private void runMigrations(Connection connection) {
        runSilently(connection, "ALTER TABLE user_coupons MODIFY COLUMN discount_rate DECIMAL(4,2) NULL");
        runSilently(connection, "ALTER TABLE activity_campaigns ADD COLUMN flash_sale BOOLEAN NOT NULL DEFAULT FALSE");
        runSilently(connection, "ALTER TABLE user_coupons ADD COLUMN coupon_name VARCHAR(120)");
        runSilently(connection, "ALTER TABLE user_coupons ADD COLUMN threshold_amount DECIMAL(10, 2)");
        runSilently(connection, "ALTER TABLE user_coupons ADD COLUMN reduce_amount DECIMAL(10, 2)");
        runSilently(connection, "ALTER TABLE user_coupons ADD COLUMN expires_at DATETIME");
        runSilently(connection, "ALTER TABLE user_coupons ADD COLUMN source_type VARCHAR(30) NOT NULL DEFAULT 'CHECKIN'");
        runSilently(connection, "ALTER TABLE user_coupons ADD COLUMN source_ref_id BIGINT");
        runSilently(connection, "ALTER TABLE user_coupons ADD COLUMN quantity INT NOT NULL DEFAULT 1");
        runSilently(connection, "UPDATE user_coupons SET quantity = 1 WHERE quantity IS NULL OR quantity < 1");
        runSilently(connection, "ALTER TABLE activity_campaigns ADD COLUMN coupon_quantity INT NOT NULL DEFAULT 1");
        runSilently(connection, "UPDATE activity_campaigns SET coupon_quantity = 1 WHERE coupon_quantity IS NULL OR coupon_quantity < 1");
        runSilently(connection, "ALTER TABLE reward_mails ADD COLUMN coupon_quantity INT NOT NULL DEFAULT 1");
        runSilently(connection, "UPDATE reward_mails SET coupon_quantity = 1 WHERE coupon_quantity IS NULL OR coupon_quantity < 1");
        runSilently(connection, "ALTER TABLE coupon_codes ADD COLUMN coupon_quantity INT NOT NULL DEFAULT 1");
        runSilently(connection, "UPDATE coupon_codes SET coupon_quantity = 1 WHERE coupon_quantity IS NULL OR coupon_quantity < 1");
        runSilently(connection, "ALTER TABLE orders ADD COLUMN estimated_delivery_days INT NOT NULL DEFAULT 3");
        runSilently(connection, "ALTER TABLE orders ADD COLUMN auto_ship_at DATETIME");
        runSilently(connection, "ALTER TABLE orders ADD COLUMN pickup_code VARCHAR(20)");
        runSilently(connection, "ALTER TABLE orders ADD COLUMN receipt_confirmed_at DATETIME");
        runSilently(connection, "UPDATE orders SET pickup_code = CONCAT('A', LPAD(MOD(id, 90) + 10, 2, '0'), '-', MOD(id, 9) + 1, '-', LPAD(MOD(id * 7919, 9000) + 1000, 4, '0')) WHERE status = 'COMPLETED' AND (pickup_code IS NULL OR pickup_code = '')");
        runSilently(connection, "CREATE TABLE IF NOT EXISTS announcements (id BIGINT PRIMARY KEY AUTO_INCREMENT, title VARCHAR(120) NOT NULL, summary VARCHAR(255), content TEXT NOT NULL, category VARCHAR(20) NOT NULL DEFAULT 'GENERAL', status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', pinned BOOLEAN NOT NULL DEFAULT FALSE, related_campaign_id BIGINT, published_at DATETIME, expires_at DATETIME, created_by BIGINT, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, INDEX idx_announcements_status_pinned (status, pinned), INDEX idx_announcements_published_at (published_at))");
        runSilently(connection, "CREATE INDEX idx_products_status_stock ON products (status, stock)");
        runSilently(connection, "CREATE INDEX idx_products_created_at ON products (created_at)");
        runSilently(connection, "CREATE INDEX idx_orders_status_created_at ON orders (status, created_at)");
    }

    private void runSilently(Connection connection, String sql) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
            LOG.info("Migration applied: {}", sql);
        } catch (SQLException ex) {
            LOG.debug("Migration skipped ({}): {}", sql, ex.getMessage());
        }
    }

    private boolean alreadyInitialized(Connection connection) {
        return "true".equalsIgnoreCase(metaValue(connection, "schema_initialized"));
    }

    private String metaValue(Connection connection, String key) {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT meta_value FROM app_meta WHERE meta_key = '" + key + "'")) {
            return rs.next() ? rs.getString(1) : null;
        } catch (SQLException ex) {
            return null;
        }
    }

    private void upsertMeta(Connection connection, String key, String value) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO app_meta (meta_key, meta_value) VALUES (?, ?) ON DUPLICATE KEY UPDATE meta_value = VALUES(meta_value)")) {
            statement.setString(1, key);
            statement.setString(2, value);
            statement.executeUpdate();
        }
    }
}

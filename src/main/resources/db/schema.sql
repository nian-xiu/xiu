-- ssm-shop schema. Idempotent: safe to re-run, but combined with the
-- app_meta.schema_initialized flag the data init only runs on first boot.

CREATE TABLE IF NOT EXISTS app_meta (
    meta_key VARCHAR(80) PRIMARY KEY,
    meta_value VARCHAR(255),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    nickname VARCHAR(80) NOT NULL,
    phone VARCHAR(30),
    email VARCHAR(120),
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    coins INT NOT NULL DEFAULT 0,
    checkin_streak INT NOT NULL DEFAULT 0,
    last_checkin_date DATE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    description VARCHAR(255),
    sort_order INT NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    subtitle VARCHAR(255),
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    original_price DECIMAL(10, 2),
    stock INT NOT NULL DEFAULT 0,
    sales INT NOT NULL DEFAULT 0,
    rating DECIMAL(3, 1) NOT NULL DEFAULT 5.0,
    cover_url VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_products_category (category_id),
    INDEX idx_products_featured (featured),
    INDEX idx_products_status_stock (status, stock),
    INDEX idx_products_created_at (created_at),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS user_coupons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    coupon_type VARCHAR(30) NOT NULL,
    coupon_name VARCHAR(120),
    discount_rate DECIMAL(4, 2),
    threshold_amount DECIMAL(10, 2),
    reduce_amount DECIMAL(10, 2),
    expires_at DATETIME,
    source_type VARCHAR(30) NOT NULL DEFAULT 'CHECKIN',
    source_ref_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'UNUSED',
    quantity INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at DATETIME,
    order_id BIGINT,
    INDEX idx_user_coupons_user_status (user_id, status),
    CONSTRAINT fk_user_coupons_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    receiver VARCHAR(80) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    province VARCHAR(80) NOT NULL,
    city VARCHAR(80) NOT NULL,
    district VARCHAR(80) NOT NULL,
    detail VARCHAR(255) NOT NULL,
    default_address BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_addresses_user (user_id),
    CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS favorites (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_favorites_user_product (user_id, product_id),
    CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorites_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_blacklist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_blacklist_user_product (user_id, product_id),
    CONSTRAINT fk_blacklist_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_blacklist_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cart_user_product (user_id, product_id),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(40) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    address_snapshot VARCHAR(500) NOT NULL,
    original_amount DECIMAL(10, 2),
    total_amount DECIMAL(10, 2) NOT NULL,
    coin_used INT NOT NULL DEFAULT 0,
    coin_discount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    coupon_id BIGINT,
    coupon_discount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    estimated_delivery_days INT NOT NULL DEFAULT 3,
    auto_ship_at DATETIME,
    status VARCHAR(30) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    remark VARCHAR(255),
    pickup_code VARCHAR(20),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at DATETIME,
    shipped_at DATETIME,
    completed_at DATETIME,
    receipt_confirmed_at DATETIME,
    INDEX idx_orders_user (user_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_status_created_at (status, created_at),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(120) NOT NULL,
    cover_url VARCHAR(500),
    unit_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    INDEX idx_order_items_order (order_id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(120) NOT NULL,
    summary VARCHAR(255),
    content TEXT NOT NULL,
    category VARCHAR(20) NOT NULL DEFAULT 'GENERAL',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    pinned BOOLEAN NOT NULL DEFAULT FALSE,
    related_campaign_id BIGINT,
    published_at DATETIME,
    expires_at DATETIME,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_announcements_status_pinned (status, pinned),
    INDEX idx_announcements_published_at (published_at)
);

CREATE TABLE IF NOT EXISTS service_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    order_id BIGINT,
    message TEXT NOT NULL,
    reply TEXT,
    sender_role VARCHAR(20) NOT NULL DEFAULT 'USER',
    user_unread BOOLEAN NOT NULL DEFAULT FALSE,
    admin_unread BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    replied_at DATETIME,
    INDEX idx_service_messages_user (user_id),
    INDEX idx_service_messages_order (order_id),
    CONSTRAINT fk_service_messages_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_service_messages_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS activity_campaigns (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    reward_type VARCHAR(20) NOT NULL,
    coin_amount INT,
    coupon_type VARCHAR(30),
    coupon_name VARCHAR(120),
    discount_rate DECIMAL(4, 2),
    threshold_amount DECIMAL(10, 2),
    reduce_amount DECIMAL(10, 2),
    start_at DATETIME,
    end_at DATETIME,
    quota_limit INT NOT NULL DEFAULT 0,
    coupon_quantity INT NOT NULL DEFAULT 1,
    coupon_expiry_days INT,
    claimed_count INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    flash_sale BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_activity_campaigns_status (status)
);

CREATE TABLE IF NOT EXISTS activity_claims (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    campaign_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    claimed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_activity_claim (campaign_id, user_id),
    CONSTRAINT fk_activity_claim_campaign FOREIGN KEY (campaign_id) REFERENCES activity_campaigns(id) ON DELETE CASCADE,
    CONSTRAINT fk_activity_claim_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reward_mails (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    sender_id BIGINT,
    sender_name VARCHAR(80),
    title VARCHAR(120) NOT NULL,
    message TEXT,
    reward_type VARCHAR(20) NOT NULL,
    coin_amount INT,
    coupon_type VARCHAR(30),
    coupon_name VARCHAR(120),
    discount_rate DECIMAL(4, 2),
    threshold_amount DECIMAL(10, 2),
    reduce_amount DECIMAL(10, 2),
    coupon_quantity INT NOT NULL DEFAULT 1,
    expires_at DATETIME,
    status VARCHAR(20) NOT NULL DEFAULT 'UNREAD',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    viewed_at DATETIME,
    claimed_at DATETIME,
    INDEX idx_reward_mails_user_status (user_id, status),
    CONSTRAINT fk_reward_mails_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS coupon_codes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(40) NOT NULL UNIQUE,
    title VARCHAR(120) NOT NULL,
    reward_type VARCHAR(20) NOT NULL,
    coin_amount INT,
    coupon_type VARCHAR(30),
    coupon_name VARCHAR(120),
    discount_rate DECIMAL(4, 2),
    threshold_amount DECIMAL(10, 2),
    reduce_amount DECIMAL(10, 2),
    coupon_expiry_days INT,
    coupon_quantity INT NOT NULL DEFAULT 1,
    expires_at DATETIME,
    total_quota INT NOT NULL DEFAULT 0,
    redeemed_count INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS coupon_code_redemptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    redeemed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_coupon_code_user (code_id, user_id),
    CONSTRAINT fk_coupon_code_redemption_code FOREIGN KEY (code_id) REFERENCES coupon_codes(id) ON DELETE CASCADE,
    CONSTRAINT fk_coupon_code_redemption_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

ALTER TABLE app_users
    ADD COLUMN first_name VARCHAR(255),
    ADD COLUMN last_name VARCHAR(255),
    ADD COLUMN phone_number VARCHAR(50);

CREATE TABLE seller_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES app_users(id),
    business_name VARCHAR(255) NOT NULL,
    legal_name VARCHAR(255),
    tax_id VARCHAR(255),
    phone_number VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id),
    label VARCHAR(255) NOT NULL,
    line1 VARCHAR(255) NOT NULL,
    line2 VARCHAR(255),
    city VARCHAR(255) NOT NULL,
    region VARCHAR(255) NOT NULL,
    postal_code VARCHAR(64) NOT NULL,
    country VARCHAR(2) NOT NULL,
    phone_number VARCHAR(50),
    is_default BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE order_shipping_addresses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    line1 VARCHAR(255) NOT NULL,
    line2 VARCHAR(255),
    city VARCHAR(255) NOT NULL,
    region VARCHAR(255) NOT NULL,
    postal_code VARCHAR(64) NOT NULL,
    country VARCHAR(2) NOT NULL,
    phone_number VARCHAR(50)
);

ALTER TABLE orders
    DROP CONSTRAINT IF EXISTS orders_customer_id_fkey;

ALTER TABLE orders
    DROP COLUMN IF EXISTS customer_id;

ALTER TABLE orders
    ADD COLUMN buyer_id BIGINT REFERENCES app_users(id),
    ADD COLUMN shipping_address_id BIGINT REFERENCES order_shipping_addresses(id);

ALTER TABLE orders
    ALTER COLUMN buyer_id SET NOT NULL;

CREATE TABLE seller_orders (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    seller_id BIGINT NOT NULL REFERENCES app_users(id),
    status VARCHAR(50) NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

ALTER TABLE order_items
    ADD COLUMN seller_order_id BIGINT REFERENCES seller_orders(id);

CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES carts(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL
);

CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    user_id BIGINT NOT NULL REFERENCES app_users(id),
    order_item_id BIGINT NOT NULL REFERENCES order_items(id),
    rating INTEGER NOT NULL,
    comment VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL
);

ALTER TABLE reviews
    ADD CONSTRAINT reviews_order_item_id_key UNIQUE (order_item_id);

DROP TABLE IF EXISTS customers;

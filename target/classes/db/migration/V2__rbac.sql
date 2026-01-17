ALTER TABLE app_users
    ADD COLUMN seller_verified BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE products
    ADD COLUMN seller_id BIGINT REFERENCES app_users(id);

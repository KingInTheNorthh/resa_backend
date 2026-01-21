CREATE TABLE product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    storage_key VARCHAR(255) NOT NULL UNIQUE,
    original_filename VARCHAR(512),
    content_type VARCHAR(255) NOT NULL,
    size BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX product_images_product_id_idx ON product_images(product_id);

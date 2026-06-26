CREATE DATABASE personalization_db;

CREATE TABLE products (
    product_id   VARCHAR(64)  NOT NULL PRIMARY KEY,
    category     VARCHAR(128) NOT NULL,
    brand        VARCHAR(128) NOT NULL,
    created_at   TIMESTAMP  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP  NOT NULL DEFAULT NOW()
);

CREATE TABLE shopper_shelf_items (
    shopper_id       VARCHAR(64)  NOT NULL,
    product_id       VARCHAR(64)  NOT NULL,
    relevancy_score  DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (shopper_id, product_id),
    CONSTRAINT fk_shopper_shelf_product
        FOREIGN KEY (product_id) REFERENCES products (product_id)
);

CREATE INDEX idx_shopper_shelf_relevancy
    ON shopper_shelf_items (shopper_id, relevancy_score DESC);

CREATE INDEX idx_products_category ON products (category);
CREATE INDEX idx_products_brand ON products (brand);


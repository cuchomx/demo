-- Drop table if exists (for development)
DROP TABLE IF EXISTS products CASCADE;

-- Create a product table
CREATE TABLE products
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY (START WITH 100 INCREMENT BY 1) PRIMARY KEY,
    name        VARCHAR(255)   NOT NULL,
    description TEXT,
    price       DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    quantity    INTEGER        NOT NULL CHECK (quantity >= 0),
    category    VARCHAR(100)   NOT NULL CHECK (category IN ('Electronics', 'Clothing', 'Books', 'Home', 'Sports')),
    active      BOOLEAN        NOT NULL DEFAULT true,
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_products_category ON products (category);
CREATE INDEX idx_products_name ON products (name);

-- Add table and column comments
COMMENT ON TABLE products IS 'Product catalog table for e-commerce system';
COMMENT ON COLUMN products.id IS 'Primary key, auto-generated product identifier';
COMMENT ON COLUMN products.name IS 'Unique product name, required field';
COMMENT ON COLUMN products.description IS 'Optional product description';
COMMENT ON COLUMN products.price IS 'Product price in USD with 2 decimal places, must be positive';
COMMENT ON COLUMN products.quantity IS 'Available product quantity, cannot be negative';
COMMENT ON COLUMN products.category IS 'Product category from predefined list';
COMMENT ON COLUMN products.active IS 'Soft delete flag, true for active products';
COMMENT ON COLUMN products.created_at IS 'Timestamp when product was created';
COMMENT ON COLUMN products.updated_at IS 'Timestamp when product was last updated';


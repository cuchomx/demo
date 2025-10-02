-- Stored Procedures for Product CRUD Operations

-- Simplify dropping to avoid brittle proargtypes checks
DROP FUNCTION IF EXISTS public.sp_create_product(varchar, text, numeric, integer, varchar);

-- 1. Create Product-Stored Procedure
CREATE OR REPLACE FUNCTION sp_create_product(
    p_name VARCHAR(255),
    p_description TEXT,
    p_price DECIMAL(10, 2),
    p_quantity INTEGER,
    p_category VARCHAR(100)
)
    RETURNS TABLE
            (
                id BIGINT
            )
AS
$$
BEGIN
    -- Basic parameter validation
    IF p_name IS NULL OR length(trim(p_name)) = 0 THEN
        RAISE EXCEPTION 'Product name must not be empty';
    END IF;
    IF p_price IS NULL OR p_price <= 0 THEN
        RAISE EXCEPTION 'Price must be positive';
    END IF;
    IF p_quantity IS NULL OR p_quantity < 0 THEN
        RAISE EXCEPTION 'Quantity must be non-negative';
    END IF;

    RETURN QUERY
        INSERT INTO products (name, description, price, quantity, category)
            VALUES (p_name, p_description, p_price, p_quantity, p_category)
            RETURNING products.id::bigint;
END;
$$ LANGUAGE plpgsql;

-- 2. Get Product by ID Stored Procedure
DROP FUNCTION IF EXISTS public.sp_get_product_by_id(bigint);

-- Example with generic types and explicit casts in the SELECT
CREATE OR REPLACE FUNCTION sp_get_product_by_id(p_product_id bigint)
    RETURNS TABLE
            (
                id          bigint,
                name        text,
                description text,
                price       numeric,
                quantity    int,
                category    text,
                active      boolean,
                created_at  timestamp,
                updated_at  timestamp
            )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT p.id::bigint,
               p.name::text,
               p.description::text,
               p.price::numeric,
               p.quantity::int,
               p.category::text,
               p.active::boolean,
               p.created_at::timestamp,
               p.updated_at::timestamp
        FROM products p
        WHERE p.id = p_product_id
          AND p.active = true;
END;
$$;

-- 3. Get All Products with Pagination Stored Procedure
DROP FUNCTION IF EXISTS public.sp_get_all_products(INTEGER, INTEGER);

CREATE OR REPLACE FUNCTION sp_get_all_products(v_limit integer, v_offset integer)
    RETURNS TABLE
            (
                id          bigint,
                name        varchar(255),
                description text,
                price       numeric(10, 2),
                quantity    integer,
                category    varchar(100),
                active      boolean,
                created_at  timestamp,
                updated_at  timestamp
            )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT p.id::bigint,
               p.name::varchar(255),
               p.description::text,
               p.price::numeric(10, 2),
               p.quantity::integer,
               p.category::varchar(100),
               p.active::boolean,
               p.created_at::timestamp,
               p.updated_at::timestamp
        FROM products p
        WHERE p.active = true
        ORDER BY p.created_at DESC
        LIMIT v_limit OFFSET v_offset;
END;
$$;


-- 7. Get Product Count (for pagination)
DROP FUNCTION IF EXISTS public.sp_get_product_count();

CREATE OR REPLACE FUNCTION sp_get_product_count()
    RETURNS INTEGER AS
$$
DECLARE
    product_count INTEGER;
BEGIN
    SELECT COUNT(*)
    INTO product_count
    FROM products
    WHERE active = true;

    RETURN product_count;
END;
$$ LANGUAGE plpgsql;

-- 8. Get Product Count by Category (for pagination)
CREATE OR REPLACE FUNCTION sp_get_product_count_by_category(p_category VARCHAR(100))
    RETURNS INTEGER AS
$$
DECLARE
    product_count INTEGER;
BEGIN
    SELECT COUNT(*)
    INTO product_count
    FROM products
    WHERE category = p_category
      AND active = true;

    RETURN product_count;
END;
$$ LANGUAGE plpgsql;

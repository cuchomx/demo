package com.example.demo.biz.products.services.create.repository;

import com.example.demo.biz.products.model.jpa.entity.ProductEntity;
import com.example.demo.commons.exception.types.DatabaseOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Repository class for Product CRUD operations using stored procedures
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCreateRepository implements IProductCreateRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String SP_CREATE_PRODUCT = "SELECT sp_create_product(?, ?, ?, ?, ?)";

    @Override
    public Optional<Integer> create(ProductEntity request) {

        log.info("Creating product with name: {}", request.getName());

        if (!validateParameters(request)) {
            log.warn("Invalid parameters provided for product creation");
            return Optional.empty();
        }

        try {
            Integer id = jdbcTemplate.queryForObject(
                    SP_CREATE_PRODUCT,
                    Integer.class,
                    request.getName(),
                    request.getDescription(),
                    request.getPrice(),
                    request.getQuantity(),
                    request.getCategory()
            );

            if (id == null || id <= 0) {
                log.warn("Stored procedure returned invalid id for name: {}", request.getName());
                return Optional.empty();
            }

            log.info("Successfully created product with ID: {}", id);
            return Optional.of(id);

        } catch (DataAccessException e) {
            log.error("Failed to create product '{}': {}", request.getName(), e.getMessage(), e);
            throw new DatabaseOperationException("Failed to create product", e);
        }
    }

    private boolean validateParameters(ProductEntity request) {
        if (request == null) {
            return false;
        }
        return request.getName() != null
                && request.getPrice() != null
                && request.getQuantity() != null
                && request.getCategory() != null;
    }
}

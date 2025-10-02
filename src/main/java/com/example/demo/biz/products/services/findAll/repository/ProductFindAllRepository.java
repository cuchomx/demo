package com.example.demo.biz.products.services.findAll.repository;

import com.example.demo.biz.products.model.jpa.entity.ProductEntity;
import com.example.demo.biz.products.model.jpa.factory.ProductRowMapperFactory;
import com.example.demo.commons.exception.types.DatabaseOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductFindAllRepository implements IProductFindAllRepository {

    private final JdbcTemplate jdbcTemplate;

    private final ProductRowMapperFactory productRowMapperFactory;
    private static final String SQL_FIND_ALL = "SELECT * FROM sp_get_all_products(?, ?)";

    @Override
    public List<ProductEntity> findAll(Integer limit, Integer offset) {
        Integer effectiveLimit = (limit == null || limit <= 0) ? 10 : limit;
        Integer effectiveOffset = (offset == null || offset < 0) ? 0 : offset;
        log.info("ProductFindAllRepository::findAll - Retrieving all products with limit: {} and offset: {}", effectiveLimit, effectiveOffset);
        try {
            List<ProductEntity> products = jdbcTemplate.query(SQL_FIND_ALL, productRowMapperFactory, effectiveLimit, effectiveOffset);
            log.info("ProductFindAllRepository::findAll - Retrieved {} products (limit={}, offset={})", products.size(), effectiveLimit, effectiveOffset);
            return products;
        } catch (DataAccessException e) {
            log.error("ProductFindAllRepository::findAll - Failed to retrieve products with limit={} offset={}: {}", effectiveLimit, effectiveOffset, e.getMessage(), e);
            throw new DatabaseOperationException("Failed to retrieve products with pagination parameters", e);
        }
    }
}

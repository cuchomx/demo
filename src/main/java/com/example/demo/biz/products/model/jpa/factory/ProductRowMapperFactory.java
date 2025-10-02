package com.example.demo.biz.products.model.jpa.factory;

import com.example.demo.biz.products.model.jpa.entity.ProductEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
public class ProductRowMapperFactory implements RowMapper<ProductEntity> {

    @Override
    public ProductEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ProductEntity.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .price(rs.getBigDecimal("price"))
                .quantity(rs.getInt("quantity"))
                .category(rs.getString("category"))
                .active(rs.getBoolean("active"))
                .createdAt(convertToLocalDateTime(rs.getTimestamp("created_at")))
                .updatedAt(convertToLocalDateTime(rs.getTimestamp("updated_at")))
                .build();
    }

    private static LocalDateTime convertToLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

}

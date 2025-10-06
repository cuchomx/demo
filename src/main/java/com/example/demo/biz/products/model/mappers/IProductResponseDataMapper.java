package com.example.demo.biz.products.model.mappers;

import com.example.commons.dto.create.ProductResponseDto;
import com.example.demo.biz.products.model.jdbc.entity.ProductEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IProductResponseDataMapper extends IAbstractMapper<ProductResponseDto, ProductEntity> {
}

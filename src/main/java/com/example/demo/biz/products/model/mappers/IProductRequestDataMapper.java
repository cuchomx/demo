package com.example.demo.biz.products.model.mappers;

import com.example.commons.dto.create.ProductRequestDto;
import com.example.demo.biz.products.model.jpa.entity.ProductEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IProductRequestDataMapper extends IAbstractMapper<ProductRequestDto, ProductEntity> {
}

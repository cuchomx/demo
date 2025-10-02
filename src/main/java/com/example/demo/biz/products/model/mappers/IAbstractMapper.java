package com.example.demo.biz.products.model.mappers;

import java.util.List;

public interface IAbstractMapper<DTO, ENTITY> {

    DTO toDto(ENTITY entity);

    List<DTO> toDtoList(List<ENTITY> entityList);

    ENTITY toEntity(DTO dto);

    List<ENTITY> toEntityList(List<DTO> dtoList);

}

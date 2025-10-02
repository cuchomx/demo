package com.example.demo.biz.products.services.findAll.service;


import com.example.commons.dto.create.ProductResponseDto;

import java.util.List;

@FunctionalInterface
public interface IProductFindAllService {

    List<ProductResponseDto> execute(String correlationId, Integer limit, Integer offset);

}

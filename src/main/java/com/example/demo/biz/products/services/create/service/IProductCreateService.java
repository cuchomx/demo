package com.example.demo.biz.products.services.create.service;

import com.example.commons.dto.create.ProductRequestDto;

import java.util.Optional;

@FunctionalInterface
public interface IProductCreateService {

    Optional<Integer> execute(String correlationId, ProductRequestDto request);

}

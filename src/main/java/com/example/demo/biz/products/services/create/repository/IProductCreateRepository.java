package com.example.demo.biz.products.services.create.repository;

import com.example.demo.biz.products.model.jpa.entity.ProductEntity;

import java.util.Optional;

public interface IProductCreateRepository {

    Optional<Integer> create(ProductEntity request);

}

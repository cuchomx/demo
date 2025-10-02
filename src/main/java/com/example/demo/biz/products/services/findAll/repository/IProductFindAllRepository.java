package com.example.demo.biz.products.services.findAll.repository;

import com.example.demo.biz.products.model.jpa.entity.ProductEntity;

import java.util.List;

public interface IProductFindAllRepository {
    List<ProductEntity> findAll(Integer limit, Integer offset);

}

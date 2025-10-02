package com.example.demo.biz.products.services.findAll.controller;

import com.example.commons.dto.create.ProductResponseDto;
import com.example.demo.biz.products.services.findAll.service.IProductFindAllService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/products")
public class ProductFindAllController {

    private final IProductFindAllService findAllService;

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> findAll(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "offset", required = false) Integer offset
    ) {
        var response = findAllService.execute(
                UUID.randomUUID().toString(),
                limit,
                offset
        );
        log.info("ProductFindAllController::findAll - Found {} products", response.size());
        return ResponseEntity.ok(response);
    }
}

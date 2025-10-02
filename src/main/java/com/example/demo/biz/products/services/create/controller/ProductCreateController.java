package com.example.demo.biz.products.services.create.controller;

import com.example.commons.dto.create.ProductRequestDto;
import com.example.demo.biz.products.services.create.service.IProductCreateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductCreateController {

    private final IProductCreateService createService;

    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody ProductRequestDto request
    ) {
        log.info("ProductCreateController::create - received request: {}", request);
        var response = createService.execute(UUID.randomUUID().toString(), request);
        log.info("ProductCreateController::create - created product with id: {}", response);
        return ResponseEntity.status(CREATED).body(response);
    }

}

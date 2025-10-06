package com.example.demo.biz.products.services.findAll.service;

import com.example.commons.dto.create.ProductResponseDto;
import com.example.commons.utils.ParameterValidationUtils;
import com.example.demo.biz.products.model.jdbc.entity.ProductEntity;
import com.example.demo.biz.products.services.findAll.queue.outbound.events.ProductFindAllProducerQueueEvent;
import com.example.demo.biz.products.services.findAll.repository.IProductFindAllRepository;
import com.example.demo.commons.cache.QueueRequestCacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.utils.StringUtils;

import java.util.List;
import java.util.Objects;

import static com.example.commons.constants.RequestStatus.*;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductFindAllService implements IProductFindAllService {

    private final ObjectMapper objectMapper;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final IProductFindAllRepository repository;

    private static final Object DEFAULT_OBJECT = new Object();

    @Override
    public List<ProductResponseDto> execute(String correlationId, Integer limit, Integer offset) {

        log.debug("ProductFindAllService::execute - Finding all products with correlationId={}, limit={}, offset={}",
                correlationId, limit, offset
        );

        if (!ParameterValidationUtils.isValidParameterRequest(correlationId, DEFAULT_OBJECT)) {
            log.warn("ProductFindAllService::execute - Invalid correlationId provided");
            return List.of();
        }

        List<ProductResponseDto> response;

        try {
            QueueRequestCacheService.add(correlationId, IN_PROGRESS);
            List<ProductEntity> products = repository.findAll(limit, offset);

            log.info("ProductFindAllService::execute - Found {} products", products.size());

            response = products.stream()
                    .filter(Objects::nonNull) // valid test
                    .map(this::toDto)
                    .collect(toList());

            String message = toJson(response);

            if (StringUtils.isBlank(message)) {
                log.warn("ProductFindAllService::findAll - SQS response message is null or blank");
                throw new IllegalArgumentException("SQS response message must not be null or blank");
            }

            log.info("ProductFindAllService::findAll - publishing product find all event to queue - message: {}", message);
            applicationEventPublisher.publishEvent(new ProductFindAllProducerQueueEvent(this, correlationId, message));

            log.info("ProductFindAllService::findAll - Successfully published product find all event to queue");
            QueueRequestCacheService.update(correlationId, COMPLETED);

        } catch (Exception e) {
            log.error("ProductFindAllService::findAll - Error finding all products: {}", e.getMessage(), e);
            QueueRequestCacheService.update(correlationId, ERROR);
            throw e;
        }

        return response;
    }

    private ProductResponseDto toDto(ProductEntity e) {
        if (e == null) return null;
        log.debug("Converting product entity to DTO: id={}", e.getId());
        return ProductResponseDto.builder()
                .id(e.getId() != null ? e.getId().longValue() : null)
                .name(e.getName())
                .description(e.getDescription())
                .price(e.getPrice())
                .quantity(e.getQuantity())
                .category(e.getCategory())
                .active(e.getActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private String toJson(List<ProductResponseDto> response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException ex) {
            log.error("ProductFindAllService::execute - Failed to convert response to JSON", ex);
            return response.toString();
        }
    }
}

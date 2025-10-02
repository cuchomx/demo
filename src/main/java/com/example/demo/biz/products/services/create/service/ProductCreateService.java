package com.example.demo.biz.products.services.create.service;

import com.example.commons.dto.create.ProductRequestDto;
import com.example.commons.utils.ParameterValidationUtils;
import com.example.demo.biz.products.model.jpa.entity.ProductEntity;
import com.example.demo.biz.products.model.mappers.IProductRequestDataMapper;
import com.example.demo.biz.products.services.create.queue.outbound.events.ProductCreateProducerQueueEvent;
import com.example.demo.biz.products.services.create.repository.IProductCreateRepository;
import com.example.demo.commons.cache.QueueRequestCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.utils.StringUtils;

import java.util.Optional;

import static com.example.commons.constants.RequestStatus.*;


@Slf4j
@RequiredArgsConstructor
@Service
public class ProductCreateService implements IProductCreateService {

    private final ApplicationEventPublisher applicationEventPublisher;

    private final IProductCreateRepository productCreateRepository;
    private final IProductRequestDataMapper productDataMapper;

    @Override
    public Optional<Integer> execute(String correlationId, ProductRequestDto request) {

        if (!ParameterValidationUtils.isValidParameterRequest(correlationId, request)) {
            log.warn("ProductCreateService::create - Invalid correlationId or request provided");
            return Optional.empty();
        }

        log.info("ProductCreateService::create - creating product: correlationId: {}, product: {}",
                correlationId,
                request
        );

        Optional<Integer> createdId;

        try {
            QueueRequestCacheService.add(correlationId, IN_PROGRESS);
            ProductEntity entity = productDataMapper.toEntity(request);

            createdId = productCreateRepository
                    .create(entity)
                    .map(this::requireId);

            createdId.ifPresentOrElse(id -> {
                log.info("ProductCreateService::create - product created with id: {}", id);

                String message = String.valueOf(id);
                if (StringUtils.isBlank(message)) {
                    throw new IllegalArgumentException("SQS message must not be null or blank");
                }

                log.info("ProductCreateService::create - publishing product creation event to queue: {}", message);
                applicationEventPublisher.publishEvent(new ProductCreateProducerQueueEvent(this, correlationId, message));

                QueueRequestCacheService.update(correlationId, COMPLETED);

            }, () -> {
                QueueRequestCacheService.update(correlationId, ERROR);
                log.warn("ProductCreateService::create - product creation failed");
            });

        } catch (Exception e) {
            log.error("ProductCreateService::create - Error creating product: {}", e.getMessage(), e);
            QueueRequestCacheService.update(correlationId, ERROR);
            throw e;
        }

        return createdId;
    }

    private Integer requireId(Integer id) {
        if (id == null || id <= 0) {
            log.error("Stored procedure returned invalid product id: {}", id);
            return null;
        }
        return id;
    }

}

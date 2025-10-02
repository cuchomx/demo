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
            log.warn("ProductCreateService::execute - Invalid correlationId or request provided");
            return Optional.empty();
        }

        log.info("ProductCreateService::execute - creating product: correlationId: {}, product: {}",
                correlationId,
                request
        );

        Optional<Integer> createdId;

        try {
            QueueRequestCacheService.add(correlationId, IN_PROGRESS);
            
            ProductEntity entity = productDataMapper.toEntity(request);
            if (entity == null) {
                log.warn("ProductCreateService::execute - Mapper returned null entity for correlationId: {}", correlationId);
                QueueRequestCacheService.update(correlationId, ERROR);
                return Optional.empty();
            }

            createdId = productCreateRepository.create(entity);

            if (createdId.isPresent()) {
                Integer id = createdId.get();
                if (id <= 0) {
                    log.error("ProductCreateService::execute - Invalid created id returned: {}", id);
                    QueueRequestCacheService.update(correlationId, ERROR);
                    return Optional.empty();
                }

                log.info("ProductCreateService::execute - product created with id: {}", id);

                applicationEventPublisher.publishEvent(
                        new ProductCreateProducerQueueEvent(this, correlationId, String.valueOf(id))
                );

                QueueRequestCacheService.update(correlationId, COMPLETED);
            } else {
                QueueRequestCacheService.update(correlationId, ERROR);
                log.warn("ProductCreateService::execute - product creation failed (empty result)");
            }

        } catch (Exception e) {
            log.error("ProductCreateService::execute - Error creating product: {}", e.getMessage(), e);
            QueueRequestCacheService.update(correlationId, ERROR);
            throw e;
        }

        return createdId;
    }

}

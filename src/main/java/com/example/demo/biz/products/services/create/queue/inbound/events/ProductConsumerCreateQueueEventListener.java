package com.example.demo.biz.products.services.create.queue.inbound.events;

import com.example.commons.dto.create.ProductRequestDto;
import com.example.demo.biz.products.services.create.service.IProductCreateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductConsumerCreateQueueEventListener implements ApplicationListener<ProductConsumerCreateQueueEvent> {

    private final ObjectMapper objectMapper;

    private final IProductCreateService productCreateService;

    @Async
    @Override
    public void onApplicationEvent(ProductConsumerCreateQueueEvent event) {
        log.info("ProductCreateQueueEventListener::onApplicationEvent - Received message: {} ", event.getMessage());
        String correlationId = event.getCorrelationId();
        var dto = toProductRequestDto(event);
        productCreateService.execute(correlationId, dto);
    }

    private ProductRequestDto toProductRequestDto(ProductConsumerCreateQueueEvent event) {
        final String message = event.getMessage();
        try {
            return objectMapper.readValue(message, ProductRequestDto.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse ProductRequestDto from event message", e);
        }
    }

}

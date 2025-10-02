package com.example.demo.biz.products.services.findAll.queue.inbound.events;

import com.example.commons.dto.find.ProductFindAllRequestDto;
import com.example.demo.biz.products.services.findAll.service.IProductFindAllService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductFindAllConsumerQueueEventListener implements ApplicationListener<ProductFindAllConsumerQueueEvent> {

    private final ObjectMapper objectMapper;

    private final IProductFindAllService iProductFindAllService;

    @Async
    @Override
    public void onApplicationEvent(ProductFindAllConsumerQueueEvent event) {

        log.info("ProductFindAllQueueEventListener::onApplicationEvent - Processing event: correlationId: {}, message: {}",
                event.getCorrelationId(),
                event.getMessage()
        );

        try {
            var message = event.getMessage();
            var correlationId = event.getCorrelationId();
            var findAllRequest = objectMapper.readValue(message, ProductFindAllRequestDto.class);

            iProductFindAllService.execute(
                    correlationId,
                    findAllRequest.getLimit() == null ? 10 : findAllRequest.getLimit(),
                    findAllRequest.getOffset() == null ? 0 : findAllRequest.getOffset()
            );

            log.info("ProductFindAllQueueEventListener::onApplicationEvent - Successfully processed event - id: {}",
                    event.getCorrelationId()
            );
        } catch (Exception e) {
            log.error("ProductFindAllQueueEventListener::onApplicationEvent - Error processing event - message:{}",
                    e.getMessage(),
                    e
            );
            throw new RuntimeException("Failed to process find all products event", e);
        }
    }

}

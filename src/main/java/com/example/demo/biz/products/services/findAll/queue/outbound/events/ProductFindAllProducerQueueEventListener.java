package com.example.demo.biz.products.services.findAll.queue.outbound.events;

import com.example.demo.biz.products.services.findAll.queue.outbound.producer.IProductFindAllQueueProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductFindAllProducerQueueEventListener implements ApplicationListener<ProductFindAllProducerQueueEvent> {

    private final IProductFindAllQueueProducer iProductFindAllQueueProducer;

    @Async
    @Override
    public void onApplicationEvent(ProductFindAllProducerQueueEvent event) {

        if (!isValidInputRequest(event.getCorrelationId(), event.getMessage())) {
            log.warn("ProductFindAllProducerQueueEventListener::onApplicationEvent - Invalid input request");
            return;
        }

        try {
            var correlationId = event.getCorrelationId();
            var message = event.getMessage();

            log.info("ProductFindAllProducerQueueEventListener::onApplicationEvent - Processing event: correlationId: {}, message: {}",
                    event.getCorrelationId(),
                    event.getMessage()
            );

            iProductFindAllQueueProducer.produce(correlationId, message);

            log.info("ProductFindAllProducerQueueEventListener::onApplicationEvent - Successfully processed event - id: {}",
                    event.getCorrelationId()
            );
        } catch (Exception e) {
            log.error("ProductFindAllProducerQueueEventListener::onApplicationEvent - Error processing event - message:{}",
                    e.getMessage(),
                    e
            );
            throw new RuntimeException("Failed to process find all products event", e);
        }
    }

    private boolean isValidInputRequest(String correlationId, String message) {
        if (correlationId == null || correlationId.isBlank()) {
            log.warn("ProductFindAllProducerQueueEventListener::onApplicationEvent - createProduct - received null correlationId");
            return false;
        }
        if (message == null) {
            log.warn("ProductFindAllProducerQueueEventListener::onApplicationEvent - createProduct - received null request");
            return false;
        }
        return true;
    }

}

package com.example.demo.biz.products.services.create.queue.outbound.events;

import com.example.demo.biz.products.services.create.queue.outbound.producer.IProductCreateQueueProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductCreateProducerQueueEventListener implements ApplicationListener<ProductCreateProducerQueueEvent> {

    private final IProductCreateQueueProducer iProductCreateQueueProducer;

    @Async
    @Override
    public void onApplicationEvent(ProductCreateProducerQueueEvent event) {
        log.info("ProductProducerCreateQueueEventListener::onApplicationEvent - Received - correlationId: {}, message: {} ",
                event.getCorrelationId(),
                event.getMessage()
        );
        iProductCreateQueueProducer.produce(
                event.getCorrelationId(),
                event.getMessage()
        );
    }

}

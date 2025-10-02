package com.example.demo.biz.products.services.create.queue.inbound.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class ProductConsumerCreateQueueEvent extends ApplicationEvent {

    @Getter
    private final String correlationId;

    @Getter
    private final String message;

    public ProductConsumerCreateQueueEvent(Object source, String correlationId, String message) {
        super(source);
        this.correlationId = correlationId;
        this.message = message;
    }

}

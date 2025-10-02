package com.example.demo.biz.products.services.findAll.queue.inbound.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@EqualsAndHashCode(callSuper = false)
public class ProductFindAllConsumerQueueEvent extends ApplicationEvent {

    @Getter
    private final String correlationId;

    @Getter
    private final String message;

    public ProductFindAllConsumerQueueEvent(Object source, String correlationId, String message) {
        super(source);
        this.correlationId = correlationId;
        this.message = message;
    }
}

package com.example.demo.biz.products.services.findAll.queue.outbound.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@EqualsAndHashCode(callSuper = false)
public class ProductFindAllProducerQueueEvent extends ApplicationEvent {

    @Getter
    private final String correlationId;

    @Getter
    private final String message;

    public ProductFindAllProducerQueueEvent(Object source, String correlationId, String message) {
        super(source);
        this.correlationId = correlationId;
        this.message = message;
    }
}

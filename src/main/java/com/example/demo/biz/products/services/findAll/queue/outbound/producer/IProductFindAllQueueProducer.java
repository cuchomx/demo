package com.example.demo.biz.products.services.findAll.queue.outbound.producer;

@FunctionalInterface
public interface IProductFindAllQueueProducer {
    void produce(String correlationId, String message);
}

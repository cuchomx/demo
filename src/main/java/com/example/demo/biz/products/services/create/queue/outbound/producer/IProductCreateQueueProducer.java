package com.example.demo.biz.products.services.create.queue.outbound.producer;

@FunctionalInterface
public interface IProductCreateQueueProducer {

    void produce(String correlationId, String message);

}

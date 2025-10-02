package com.example.demo.biz.products.services.create.queue.inbound.consumer;

public interface IProductCreateQueueConsumer {

    void consume();

    void delete(String receiptHandle);

}

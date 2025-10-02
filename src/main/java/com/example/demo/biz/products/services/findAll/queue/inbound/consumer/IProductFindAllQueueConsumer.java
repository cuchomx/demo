package com.example.demo.biz.products.services.findAll.queue.inbound.consumer;

public interface IProductFindAllQueueConsumer {

    void consume();

    void delete(String receiptHandle);

}

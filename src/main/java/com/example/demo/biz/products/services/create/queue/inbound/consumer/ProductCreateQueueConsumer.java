package com.example.demo.biz.products.services.create.queue.inbound.consumer;

import com.example.commons.utils.ParameterValidationUtils;
import com.example.commons.utils.QueueAttributeUtils;
import com.example.commons.utils.ReceiveMessageQueueUtils;
import com.example.demo.biz.products.services.create.queue.inbound.events.ProductConsumerCreateQueueEvent;
import com.example.demo.commons.cache.QueueRequestCacheService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.utils.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductCreateQueueConsumer implements IProductCreateQueueConsumer {

    private final ApplicationEventPublisher applicationEventPublisher;

    private final SqsClient sqsClient;

    @Value("${aws.sqs.queue.create.service.consumer.url}")
    private String queueUrl;

    private static final int MAX_NUMBER_OF_MESSAGES = 10;
    private static final int WAIT_TIME_SECONDS = 20;

    @PostConstruct
    void validateConfiguration() {

        if (StringUtils.isBlank(queueUrl)) {
            throw new IllegalStateException("aws.sqs.queue.create.url must be configured");
        }
        try {
            var uri = new URI(queueUrl);
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalStateException("Invalid SQS queue URL scheme: " + uri.getScheme());
            }
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid SQS queue URL: " + queueUrl, e);
        }
    }

    @Scheduled(fixedDelayString = "${sqs.poll.fixedDelay.ms:1000}")
    @Override
    public void consume() {
        log.debug("===========================================================================");
        log.debug("ProductCreateQueueConsumer::consume - Polling SQS queue {}", queueUrl);

        ReceiveMessageRequest receiveRequest = ReceiveMessageQueueUtils.buildReceiveRequest(queueUrl, "All");
        List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

        if (messages == null || messages.isEmpty()) {
            log.debug("ProductCreateQueueConsumer::consume - No messages received");
            return;
        }

        for (Message m : messages) {
            QueueAttributeUtils.logMessageSummary(m);

            String correlationId = QueueAttributeUtils.extractCorrelationId(m);
            if (!ParameterValidationUtils.isValidCorrelationIdValue(correlationId)) {
                log.warn("ProductCreateQueueConsumer::consume - Missing correlationId for messageId={}. correlationId:{}.", m.messageId(), correlationId);
                delete(m.receiptHandle());
                continue;
            }

            boolean alreadyProcessed = QueueRequestCacheService.containsKey(correlationId);
            if (alreadyProcessed) {
                log.debug("ProductCreateQueueConsumer::consume - Message with messageId={} correlationId={} already processed (cached). Deleting.",
                        m.messageId(), correlationId);
                delete(m.receiptHandle());
                continue;
            }

            try {
                applicationEventPublisher.publishEvent(new ProductConsumerCreateQueueEvent(this, correlationId, m.body()));
                delete(m.receiptHandle());
            } catch (RuntimeException ex) {
                log.error("ProductCreateQueueConsumer::consume - Processing failed for messageId={} correlationId={}. Will NOT delete for retry. Error: {}",
                        m.messageId(), correlationId, ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void delete(String receiptHandle) {
        if (StringUtils.isBlank(receiptHandle)) {
            log.warn("ProductCreateQueueConsumer::delete - Skipping delete due to empty receiptHandle");
            return;
        }
        try {
            var deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(receiptHandle)
                    .build();
            sqsClient.deleteMessage(deleteRequest);
            log.info("ProductCreateQueueConsumer::delete - Deleted message with receiptHandle={}", receiptHandle);
        } catch (RuntimeException ex) {
            log.error("ProductCreateQueueConsumer::safeDelete - Failed to delete message. Error: {}", ex.getMessage(), ex);
        }
    }

}

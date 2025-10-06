package com.example.demo.biz.products.services.create.queue.inbound.consumer;

import com.example.commons.constants.RequestStatus;
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

    @PostConstruct
    void validateConfiguration() {
        if (StringUtils.isBlank(queueUrl)) {
            throw new IllegalStateException("aws.sqs.queue.create.service.consumer.url must be configured");
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
        log.trace("ProductCreateQueueConsumer::consume - Polling SQS queue {}", queueUrl);

        ReceiveMessageRequest receiveRequest = ReceiveMessageQueueUtils.buildReceiveRequest(queueUrl, "All");

        List<Message> messages;
        try {
            messages = sqsClient.receiveMessage(receiveRequest).messages();
        } catch (RuntimeException ex) {
            log.error("ProductCreateQueueConsumer::consume - Failed to receive messages. Error: {}", ex.getMessage(), ex);
            return;
        }

        if (messages == null || messages.isEmpty()) {
            log.debug("ProductCreateQueueConsumer::consume - No messages received");
            return;
        }

        for (Message m : messages) {
            try {
                QueueAttributeUtils.logMessageSummary(m);
            } catch (RuntimeException ex) {
                log.warn("ProductCreateQueueConsumer::consume - Failed to log message summary for messageId={}. Error: {}", m.messageId(), ex.getMessage(), ex);
            }

            String correlationId;
            try {
                correlationId = QueueAttributeUtils.extractCorrelationId(m);
            } catch (RuntimeException ex) {
                log.warn("ProductCreateQueueConsumer::consume - Failed to extract correlationId for messageId={}. Deleting. Error: {}",
                        m.messageId(), ex.getMessage(), ex);
                delete(m.receiptHandle());
                continue;
            }

            if (!ParameterValidationUtils.isValidCorrelationIdValue(correlationId)) {
                log.warn("ProductCreateQueueConsumer::consume - Missing/invalid correlationId for messageId={}. Deleting.", m.messageId());
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

            QueueRequestCacheService.add(correlationId, RequestStatus.IN_PROGRESS);

            try {
                applicationEventPublisher.publishEvent(new ProductConsumerCreateQueueEvent(this, correlationId, m.body()));
                QueueRequestCacheService.update(correlationId, RequestStatus.COMPLETED);
                delete(m.receiptHandle());
            } catch (RuntimeException ex) {
                QueueRequestCacheService.update(correlationId, RequestStatus.ERROR);
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
            log.debug("ProductCreateQueueConsumer::delete - Deleted message");
        } catch (RuntimeException ex) {
            log.error("ProductCreateQueueConsumer::delete - Failed to delete message. Error: {}", ex.getMessage(), ex);
        }
    }

}

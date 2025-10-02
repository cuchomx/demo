package com.example.demo.biz.products.services.findAll.queue.inbound.consumer;

import com.example.commons.utils.ParameterValidationUtils;
import com.example.commons.utils.QueueAttributeUtils;
import com.example.commons.utils.ReceiveMessageQueueUtils;
import com.example.demo.biz.products.services.findAll.queue.inbound.events.ProductFindAllConsumerQueueEvent;
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
public class ProductFindAllQueueConsumer implements IProductFindAllQueueConsumer {

    private final ApplicationEventPublisher applicationEventPublisher;

    private final SqsClient sqsClient;

    @Value("${aws.sqs.queue.find.service.consumer.url}")
    private String queueUrl;

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

    @Scheduled(cron = "${sqs.poll.cron:*/1 * * * * *}")
    @Override
    public void consume() {
        log.info("===========================================================================");
        log.debug("ProductFindAllQueueConsumer::consume - Polling SQS queue {}", queueUrl);

        ReceiveMessageRequest receiveRequest = ReceiveMessageQueueUtils.buildReceiveRequest(queueUrl, "All");
        List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

        if (messages == null || messages.isEmpty()) {
            log.debug("ProductFindAllQueueConsumer::consume - No messages received");
            return;
        }

        for (Message m : messages) {

            QueueAttributeUtils.logMessageSummary(m);

            String correlationId = QueueAttributeUtils.extractCorrelationId(m);
            if (!ParameterValidationUtils.isValidCorrelationIdValue(correlationId)) {
                log.warn("ProductFindAllQueueConsumer::consume - Missing correlationId for messageId={}. correlationId:{}", m.messageId(), correlationId);
                delete(m.receiptHandle());
                continue;
            }

            boolean alreadyProcessed = QueueRequestCacheService.containsKey(correlationId);
            if (alreadyProcessed) {
                log.debug("ProductFindAllQueueConsumer::consume - Message with messageId={} correlationId={} already processed (cached). Deleting.", m.messageId(), correlationId);
                delete(m.receiptHandle());
                continue;
            }

            try {
                applicationEventPublisher.publishEvent(new ProductFindAllConsumerQueueEvent(this, correlationId, m.body()));
                delete(m.receiptHandle());
            } catch (RuntimeException x) {
                log.error("ProductFindAllQueueConsumer::consume - Processing failed for messageId={} correlationId={}. Will NOT delete for retry. Error: {}",
                        m.messageId(),
                        correlationId,
                        x.getMessage(),
                        x
                );
            }
        }
    }

    @Override
    public void delete(String receiptHandle) {
        if (StringUtils.isBlank(receiptHandle)) {
            log.warn("ProductFindAllQueueConsumer::delete - Skipping delete due to empty receiptHandle");
            return;
        }
        try {
            var deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(receiptHandle)
                    .build();
            sqsClient.deleteMessage(deleteRequest);
            log.info("ProductFindAllQueueConsumer::delete - Deleted message with receiptHandle={}", receiptHandle);
        } catch (RuntimeException ex) {
            log.error("ProductFindAllQueueConsumer::delete - Failed to delete message. Error: {}", ex.getMessage(), ex);
        }
    }

}

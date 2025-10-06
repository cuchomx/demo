package com.example.demo.biz.products.services.findAll.queue.outbound.producer;

import com.example.commons.utils.SendMessageQueueUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

import java.net.URI;
import java.net.URISyntaxException;

@RequiredArgsConstructor
@Slf4j
@Component
public class ProductFindAllQueueProducer implements IProductFindAllQueueProducer {

    private final SqsClient sqsClient;

    @Value("${aws.sqs.queue.find.service.producer.url}")
    private String queueUrl;

    @PostConstruct
    void validateConfiguration() {
        if (queueUrl == null || queueUrl.isBlank()) {
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

        try {
            var attrRequest = GetQueueAttributesRequest.builder()
                    .queueUrl(queueUrl)
                    .attributeNames(QueueAttributeName.QUEUE_ARN)
                    .build();
            var attrs = sqsClient.getQueueAttributes(attrRequest);
            log.info("Validated SQS queue exists. ARN={}", attrs.attributes().get(QueueAttributeName.QUEUE_ARN));
        } catch (AwsServiceException | SdkClientException ex) {
            throw new IllegalStateException("Failed to access SQS queue at configured URL (does it exist in the configured region/account?): " + queueUrl, ex);
        }
    }

    @Override
    public void produce(String correlationId, String message) {

        log.info("ProductQueueFindAllQueueProducer::produce - Producing list - correlationId: {}, message: {}",
                correlationId,
                message
        );

        try {
            var sendRequest = SendMessageQueueUtils.buildSendMessageRequest(queueUrl, correlationId, message);
            log.info("ProductQueueFindAllQueueProducer::produce - Sending message to queue: {}, correlationId: {}, sendRequest: {}, messageBody: {}",
                    sendRequest.queueUrl(),
                    correlationId,
                    sendRequest,
                    sendRequest.messageBody()
            );

            var response = sqsClient.sendMessage(sendRequest);
            log.info("ProductQueueFindAllQueueProducer::produce - Message sent successfully. queue:{}, correlationId:{}, messageId={}, sequenceNumber={}, message:{}",
                    queueUrl,
                    correlationId,
                    response.messageId(),
                    response.sequenceNumber(),
                    message
            );
        } catch (AwsServiceException e) {
            log.error("ProductQueueFindAllQueueProducer::produce - AWS service error. statusCode={}, awsErrorCode={}, requestId={}, message={}",
                    e.statusCode(), e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "n/a",
                    e.requestId(), e.getMessage(), e);
            throw e;
        } catch (SdkClientException e) {
            log.error("ProductQueueFindAllQueueProducer::produce - SDK client error: {}", e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            log.error("ProductQueueFindAllQueueProducer::produce - Unexpected error", e);
            throw e;
        }
    }

}

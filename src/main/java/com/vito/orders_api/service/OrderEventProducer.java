package com.vito.orders_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vito.orders_api.config.SqsProperties;
import com.vito.orders_api.dto.OrderEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventProducer {

    private final SqsClient sqsClient;
    private final SqsProperties sqsProperties;
    private final ObjectMapper objectMapper;

    public void sendOrderEvent(OrderEventDTO event) {
        try {
            String messageBody = objectMapper.writeValueAsString(event);

            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(sqsProperties.getQueueUrl())
                    .messageBody(messageBody)
                    .build());

            log.info("Order event sent to SQS: {} - {}", event.getEventType(), event.getOrderId());

        } catch (Exception e) {
            log.error("Failed to send order event to SQS: {}", e.getMessage(), e);
        }
    }
}
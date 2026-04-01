package com.vito.orders_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vito.orders_api.config.SnsProperties;
import com.vito.orders_api.config.SqsProperties;
import com.vito.orders_api.dto.OrderEventDTO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final SqsClient sqsClient;
    private final SnsClient snsClient;
    private final SqsProperties sqsProperties;
    private final SnsProperties snsProperties;
    private final ObjectMapper objectMapper;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void startPolling() {
        scheduler.scheduleWithFixedDelay(this::pollMessages, 5, 10, TimeUnit.SECONDS);
        log.info("SQS polling started for queue: {}", sqsProperties.getQueueUrl());
    }

    @PreDestroy
    public void stopPolling() {
        scheduler.shutdown();
        log.info("SQS polling stopped");
    }

    private void pollMessages() {
        try {
            List<Message> messages = sqsClient.receiveMessage(
                    ReceiveMessageRequest.builder()
                            .queueUrl(sqsProperties.getQueueUrl())
                            .maxNumberOfMessages(10)
                            .waitTimeSeconds(5)
                            .build()
            ).messages();

            for (Message message : messages) {
                processMessage(message);
            }

        } catch (Exception e) {
            log.error("Error polling SQS messages: {}", e.getMessage(), e);
        }
    }

    private void processMessage(Message message) {
        try {
            OrderEventDTO event = objectMapper.readValue(message.body(), OrderEventDTO.class);
            log.info("Processing order event: {} - {}", event.getEventType(), event.getOrderId());

            String notification = buildNotification(event);
            String subject = buildSubject(event);

            snsClient.publish(PublishRequest.builder()
                    .topicArn(snsProperties.getTopicArn())
                    .subject(subject)
                    .message(notification)
                    .build());

            log.info("Notification published to SNS for order: {}", event.getOrderId());

            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(sqsProperties.getQueueUrl())
                    .receiptHandle(message.receiptHandle())
                    .build());

        } catch (Exception e) {
            log.error("Error processing SQS message: {}", e.getMessage(), e);
        }
    }

    private String buildSubject(OrderEventDTO event) {
        return switch (event.getEventType()) {
            case "ORDER_CREATED" -> "Novo pedido criado #" + event.getOrderId();
            case "ORDER_STATUS_UPDATED" -> "Status do pedido atualizado #" + event.getOrderId();
            default -> "Atualização do pedido #" + event.getOrderId();
        };
    }

    private String buildNotification(OrderEventDTO event) {
        return switch (event.getEventType()) {
            case "ORDER_CREATED" -> String.format("""
                    Olá %s!
                    
                    Seu pedido foi criado com sucesso!
                    
                    Pedido: %s
                    Total: R$ %.2f
                    Status: %s
                    
                    Obrigado pela sua compra!
                    """,
                    event.getCustomerName(),
                    event.getOrderId(),
                    event.getTotalAmount(),
                    event.getStatus());

            case "ORDER_STATUS_UPDATED" -> String.format("""
                    Olá %s!
                    
                    O status do seu pedido foi atualizado!
                    
                    Pedido: %s
                    Novo status: %s
                    Total: R$ %.2f
                    """,
                    event.getCustomerName(),
                    event.getOrderId(),
                    event.getStatus(),
                    event.getTotalAmount());

            default -> "Atualização no pedido: " + event.getOrderId();
        };
    }
}
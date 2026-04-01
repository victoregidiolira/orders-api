package com.vito.orders_api.service;

import com.vito.orders_api.domain.Customer;
import com.vito.orders_api.domain.Order;
import com.vito.orders_api.domain.OrderItem;
import com.vito.orders_api.domain.OrderStatus;
import com.vito.orders_api.dto.OrderEventDTO;
import com.vito.orders_api.dto.OrderItemResponseDTO;
import com.vito.orders_api.dto.OrderRequestDTO;
import com.vito.orders_api.dto.OrderResponseDTO;
import com.vito.orders_api.exception.ResourceNotFoundException;
import com.vito.orders_api.repository.CustomerRepository;
import com.vito.orders_api.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderEventProducer eventProducer;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public OrderResponseDTO create(OrderRequestDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + dto.getCustomerId()));

        LocalDateTime now = LocalDateTime.now();

        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PENDING)
                .notes(dto.getNotes())
                .totalAmount(BigDecimal.ZERO)
                .createdAt(now)
                .updatedAt(now)
                .build();

        List<OrderItem> items = dto.getItems().stream()
                .map(itemDto -> OrderItem.builder()
                        .order(order)
                        .productName(itemDto.getProductName())
                        .quantity(itemDto.getQuantity())
                        .unitPrice(itemDto.getUnitPrice())
                        .createdAt(now)
                        .build())
                .toList();

        order.getItems().addAll(items);

        BigDecimal total = items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(total);

        OrderResponseDTO response = toResponse(orderRepository.save(order));

        eventProducer.sendOrderEvent(OrderEventDTO.builder()
                .orderId(response.getId())
                .customerId(response.getCustomerId())
                .customerName(response.getCustomerName())
                .customerEmail(customer.getEmail())
                .status(response.getStatus())
                .totalAmount(response.getTotalAmount())
                .eventType("ORDER_CREATED")
                .build());

        return response;
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> findAll() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO findById(UUID id) {
        return orderRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> findByCustomerId(UUID customerId) {
        return orderRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponseDTO updateStatus(UUID id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        order.setStatus(newStatus);  // ← move para ANTES do save

        Order saved = orderRepository.save(order);
        OrderResponseDTO response = toResponse(saved);

        eventProducer.sendOrderEvent(OrderEventDTO.builder()
                .orderId(response.getId())
                .customerId(response.getCustomerId())
                .customerName(response.getCustomerName())
                .customerEmail(saved.getCustomer().getEmail())
                .status(response.getStatus())
                .totalAmount(response.getTotalAmount())
                .eventType("ORDER_STATUS_UPDATED")
                .build());

        return response;
    }

    @Transactional
    public void delete(UUID id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order not found: " + id);
        }
        orderRepository.deleteById(id);
    }

    private final S3Service s3Service;

    @Transactional
    public OrderResponseDTO uploadAttachment(UUID id, MultipartFile file) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        if (order.getAttachmentUrl() != null) {
            s3Service.deleteFile(order.getAttachmentUrl());
        }

        String key = s3Service.uploadFile(file, "orders/" + id);
        order.setAttachmentUrl(key);

        return toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public String getAttachmentPresignedUrl(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        if (order.getAttachmentUrl() == null) {
            throw new ResourceNotFoundException("Order has no attachment: " + id);
        }

        return s3Service.generatePresignedUrl(order.getAttachmentUrl());
    }

    private OrderResponseDTO toResponse(Order order) {
        List<OrderItemResponseDTO> itemDtos = order.getItems().stream()
                .map(item -> OrderItemResponseDTO.builder()
                        .id(item.getId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        return OrderResponseDTO.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .notes(order.getNotes())
                .items(itemDtos)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .attachmentUrl(order.getAttachmentUrl())
                .build();
    }
}
package com.vito.orders_api.controller;

import com.vito.orders_api.domain.OrderStatus;
import com.vito.orders_api.dto.OrderRequestDTO;
import com.vito.orders_api.dto.OrderResponseDTO;
import com.vito.orders_api.service.OrderService;
import com.vito.orders_api.service.S3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@Valid @RequestBody OrderRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> findAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponseDTO>> findByCustomerId(@PathVariable UUID customerId) {
        return ResponseEntity.ok(orderService.findByCustomerId(customerId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateStatus(@PathVariable UUID id,
                                                         @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private final S3Service s3Service;

    @PostMapping(value = "/{id}/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OrderResponseDTO> uploadAttachment(@PathVariable UUID id,
                                                             @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(orderService.uploadAttachment(id, file));
    }

    @GetMapping("/{id}/attachment/url")
    public ResponseEntity<String> getAttachmentUrl(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getAttachmentPresignedUrl(id));
    }

}
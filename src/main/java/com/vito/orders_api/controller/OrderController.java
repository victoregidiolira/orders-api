package com.vito.orders_api.controller;

import com.vito.orders_api.domain.OrderStatus;
import com.vito.orders_api.dto.OrderRequestDTO;
import com.vito.orders_api.dto.OrderResponseDTO;
import com.vito.orders_api.service.OrderService;
import com.vito.orders_api.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Pedidos", description = "Gerenciamento de pedidos com notificações automáticas via SQS/SNS")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Criar pedido", description = "Cria um pedido e envia notificação por email via SQS/SNS")
    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@Valid @RequestBody OrderRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(dto));
    }

    @Operation(summary = "Listar pedidos")
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> findAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @Operation(summary = "Buscar pedido por ID")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @Operation(summary = "Pedidos por cliente")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponseDTO>> findByCustomerId(@PathVariable UUID customerId) {
        return ResponseEntity.ok(orderService.findByCustomerId(customerId));
    }

    @Operation(summary = "Atualizar status", description = "Atualiza o status e envia notificação por email")
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateStatus(@PathVariable UUID id,
                                                         @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }

    @Operation(summary = "Remover pedido")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private final S3Service s3Service;

    @Operation(summary = "Upload de anexo", description = "Faz upload de arquivo para o AWS S3")
    @PostMapping(value = "/{id}/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OrderResponseDTO> uploadAttachment(@PathVariable UUID id,
                                                             @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(orderService.uploadAttachment(id, file));
    }

    @Operation(summary = "URL do anexo", description = "Gera URL assinada (presigned URL) válida por 60 minutos")
    @GetMapping("/{id}/attachment/url")
    public ResponseEntity<String> getAttachmentUrl(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getAttachmentPresignedUrl(id));
    }

}
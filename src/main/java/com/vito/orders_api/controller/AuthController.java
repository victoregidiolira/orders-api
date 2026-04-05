package com.vito.orders_api.controller;

import com.vito.orders_api.dto.AuthRequestDTO;
import com.vito.orders_api.dto.AuthResponseDTO;
import com.vito.orders_api.dto.RegisterRequestDTO;
import com.vito.orders_api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Registro e login de usuários via AWS Cognito")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registrar usuário", description = "Cria um novo usuário no AWS Cognito")
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequestDTO dto) {
        authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Login", description = "Autentica o usuário e retorna tokens JWT")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}
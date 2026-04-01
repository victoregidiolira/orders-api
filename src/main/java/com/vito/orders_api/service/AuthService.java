package com.vito.orders_api.service;

import com.vito.orders_api.config.CognitoProperties;
import com.vito.orders_api.dto.AuthRequestDTO;
import com.vito.orders_api.dto.AuthResponseDTO;
import com.vito.orders_api.dto.RegisterRequestDTO;
import com.vito.orders_api.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final CognitoIdentityProviderClient cognitoClient;
    private final CognitoProperties cognitoProperties;

    public void register(RegisterRequestDTO dto) {
        try {
            cognitoClient.signUp(SignUpRequest.builder()
                    .clientId(cognitoProperties.getClientId())
                    .username(dto.getEmail())
                    .password(dto.getPassword())
                    .userAttributes(
                            AttributeType.builder().name("email").value(dto.getEmail()).build(),
                            AttributeType.builder().name("name").value(dto.getName()).build()
                    )
                    .build());

            // Auto-confirma o usuário (sem verificação de email por enquanto)
            cognitoClient.adminConfirmSignUp(AdminConfirmSignUpRequest.builder()
                    .userPoolId(cognitoProperties.getUserPoolId())
                    .username(dto.getEmail())
                    .build());

            log.info("User registered successfully: {}", dto.getEmail());

        } catch (UsernameExistsException e) {
            throw new BusinessException("Email already registered: " + dto.getEmail());
        } catch (InvalidPasswordException e) {
            throw new BusinessException("Password does not meet requirements: " + e.getMessage());
        }
    }

    public AuthResponseDTO login(AuthRequestDTO dto) {
        try {
            InitiateAuthResponse response = cognitoClient.initiateAuth(
                    InitiateAuthRequest.builder()
                            .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                            .clientId(cognitoProperties.getClientId())
                            .authParameters(Map.of(
                                    "USERNAME", dto.getEmail(),
                                    "PASSWORD", dto.getPassword()
                            ))
                            .build());

            AuthenticationResultType result = response.authenticationResult();

            return AuthResponseDTO.builder()
                    .accessToken(result.accessToken())
                    .idToken(result.idToken())
                    .refreshToken(result.refreshToken())
                    .expiresIn(result.expiresIn())
                    .tokenType(result.tokenType())
                    .build();

        } catch (NotAuthorizedException e) {
            throw new BusinessException("Invalid email or password");
        } catch (UserNotFoundException e) {
            throw new BusinessException("Invalid email or password");
        }
    }
}
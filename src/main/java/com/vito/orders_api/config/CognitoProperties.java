package com.vito.orders_api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aws.cognito")
public class CognitoProperties {
    private String userPoolId;
    private String clientId;
    private String region;
}
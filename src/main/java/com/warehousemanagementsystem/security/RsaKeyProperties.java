package com.warehousemanagementsystem.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;

@ConfigurationProperties(prefix = "jwt")
public record RsaKeyProperties(
    RSAPublicKey publicKey, 
    RSAPrivateKey privateKey,
    long expirationMinutes
) {}
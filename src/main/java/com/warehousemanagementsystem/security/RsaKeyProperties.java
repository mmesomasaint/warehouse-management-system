package com.warehousemanagementsystem.security;

@ConfigurationProperties(prefix = "jwt")
public record RsaKeyProperties(
    RSAPublicKey publicKey, 
    RSAPrivateKey privateKey,
    long expirationMinutes
) {}
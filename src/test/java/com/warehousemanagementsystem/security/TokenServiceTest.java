package com.warehousemanagementsystem.security;

import com.warehousemanagementsystem.model.Employee;
import com.warehousemanagementsystem.model.Store;
import com.warehousemanagementsystem.model.security.EmployeeRole;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private RsaKeyProperties rsaKeyProperties;

    @Mock
    private Authentication authentication;

    private TokenService tokenService;
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() throws Exception {
        // 1. Generate a real ephemeral RSA key pair for cryptographic testing
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        // 2. Configure mock RsaKeyProperties
        when(rsaKeyProperties.publicKey()).thenReturn(publicKey);
        when(rsaKeyProperties.privateKey()).thenReturn(privateKey);
        when(rsaKeyProperties.expirationMinutes()).thenReturn(60L);

        // 3. Setup real Nimbus JwtEncoder and JwtDecoder using the generated keys
        JWK jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        NimbusJwtEncoder jwtEncoder = new NimbusJwtEncoder(jwks);
        
        jwtDecoder = NimbusJwtDecoder.withPublicKey(publicKey).build();

        // 4. Instantiate TokenService with real encoder
        tokenService = new TokenService(jwtEncoder, rsaKeyProperties);

        // 5. Setup mock employee principal
        Store store = new Store();
        UUID storeId = UUID.randomUUID();
        store.setStoreId(storeId);

        Employee employee = new Employee();
        employee.setEmployeeId(10L);
        employee.setEmail("manager@warehouse.com");
        employee.setRole(EmployeeRole.STORE_MANAGER);
        employee.setStore(store);

        EmployeePrincipal principal = new EmployeePrincipal(employee);
        when(authentication.getPrincipal()).thenReturn(principal);
        doReturn(employee.getRole().getAuthorities())
            .when(authentication)
            .getAuthorities();
    }

    @Test
    void shouldGenerateAndDecodeValidTokenWithClaims() {
        // Act
        String tokenValue = tokenService.generateToken(authentication);

        // Assert Token is generated
        assertThat(tokenValue).isNotNull();

        // Decode token using Nimbus JwtDecoder to verify claims
        Jwt decodedJwt = jwtDecoder.decode(tokenValue);

        assertThat(decodedJwt.getSubject()).isEqualTo("manager@warehouse.com");
        assertThat(decodedJwt.<Long>getClaim("employeeId")).isEqualTo(10);
        assertThat((String) decodedJwt.getClaim("scope")).contains("sales:checkout");
        assertThat(decodedJwt.getExpiresAt()).isNotNull();
    }
}
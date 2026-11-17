package com.warehousemanagementsystem.security;

import com.warehousemanagementsystem.model.Employee;
import com.warehousemanagementsystem.model.Store;
import com.warehousemanagementsystem.model.security.EmployeeRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @Mock
    private RsaKeyProperties rsaKeyProperties;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;
    private EmployeePrincipal principal;

    @BeforeEach
    void setUp() throws Exception {
        // Generate a real ephemeral RSA key pair for testing cryptography
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();

        // Setup mock RsaKeyProperties record accessors
        when(rsaKeyProperties.privateKey()).thenReturn(privateKey);
        when(rsaKeyProperties.publicKey()).thenReturn(publicKey);
        when(rsaKeyProperties.expirationMinutes()).thenReturn(60L);

        // Setup mock employee and principal
        Store store = new Store();
        store.setStoreId(UUID.randomUUID());

        Employee employee = new Employee();
        employee.setEmployeeId(10L);
        employee.setEmail("manager@warehouse.com");
        employee.setRole(EmployeeRole.STORE_MANAGER);
        employee.setStore(store);

        principal = new EmployeePrincipal(employee);
    }

    @Test
    void shouldGenerateAndValidateTokenSuccessfully() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(principal);

        // Act
        String token = jwtTokenProvider.generateToken(authentication);

        // Assert
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromJWT(token)).isEqualTo("manager@warehouse.com");
    }

    @Test
    void shouldReturnFalseWhenTokenIsInvalid() {
        // Arrange
        String malformedToken = "eyJhbGciOiJSUzI1NiJ9.malformed.signature";

        // Act & Assert
        assertThat(jwtTokenProvider.validateToken(malformedToken)).isFalse();
    }
}
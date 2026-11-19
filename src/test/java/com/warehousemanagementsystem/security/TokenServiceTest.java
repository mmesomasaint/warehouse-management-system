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
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import java.util.List;

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
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) pair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) pair.getPrivate();

        RsaKeyProperties rsaKeys = new RsaKeyProperties(publicKey, privateKey, 60L);

        JWK jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
        JwtEncoder realEncoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
        jwtDecoder = NimbusJwtDecoder.withPublicKey(publicKey).build();

        tokenService = new TokenService(realEncoder, rsaKeys);

        // Setup mock employee principal
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
        assertThat(decodedJwt.<Long>getClaim("employeeId")).isEqualTo(10L);
        assertThat(decodedJwt.<List<String>>getClaim("authorities")).contains("sales:checkout", "ROLE_STORE_MANAGER");
        assertThat(decodedJwt.getExpiresAt()).isNotNull();
    }
}
package com.warehousemanagementsystem.service;

@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final RsaKeyProperties rsaKeys;

    public TokenService(JwtEncoder encoder, RsaKeyProperties rsaKeys) {
        this.encoder = encoder;
        this.rsaKeys = rsaKeys;
    }

    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        String scope = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plus(rsaKeys.expirationMinutes(), ChronoUnit.MINUTES))
            .subject(authentication.getName())
            .claim("scope", scope)
            .build();

        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}

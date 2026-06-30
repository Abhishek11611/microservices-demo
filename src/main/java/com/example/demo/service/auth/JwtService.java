package com.example.demo.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    public JwtService(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    private static final String KEY_ID = "key-2026-1";
    private static final long ACCESS_TOKEN_TTL_MIN = 15;

    public String generateAccessToken (String userName){
        return generateToken(userName);
    }


    public String generateToken(String userName) {
        Instant now = Instant.now();

        return Jwts.builder()
                .header().keyId(KEY_ID).and()
                .subject(userName)
                .claim("type", "access")
                .issuer("your-bank-service")
                .audience().add("your-bank-clients").and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ACCESS_TOKEN_TTL_MIN, ChronoUnit.MINUTES)))
                .id(UUID.randomUUID().toString()) // jti, useful for revocation/blacklist
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public Jws<Claims> parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .requireIssuer("your-bank-service")
                .build()
                .parseSignedClaims(token);
    }

    public String extractUsername(String token) {
        return parseAndValidate(token).getPayload().getSubject();
    }
}

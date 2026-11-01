package com.example.demo.service.security;

import com.example.demo.exceptions.UnauthorisedException;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private KeyPair signingKeyPair;
    private KeyPair encryptionKeyPair;
    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        signingKeyPair = generateRsaKeyPair();
        encryptionKeyPair = generateRsaKeyPair();
        jwtService = createJwtService(signingKeyPair, encryptionKeyPair);
    }

    @Test
    void generateAccessToken_andParseAndValidate_roundTripsClaims() throws Exception {
        String token = jwtService.generateAccessToken("john@example.com", "USER", 42L, "UABC123");

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(5);

        JWTClaimsSet claims = jwtService.parseAndValidate(token);

        assertThat(claims.getSubject()).isEqualTo("john@example.com");
        assertThat(claims.getStringClaim("roles")).isEqualTo("USER");
        assertThat(((Number) claims.getClaim("userId")).longValue()).isEqualTo(42L);
        assertThat(claims.getStringClaim("userCode")).isEqualTo("UABC123");
        assertThat(claims.getStringClaim("type")).isEqualTo("access");
        assertThat(claims.getIssuer()).isEqualTo("demo-service");
        assertThat(claims.getAudience()).containsExactly("demo-clients");
        assertThat(claims.getJWTID()).isNotBlank();
        assertThat(claims.getExpirationTime()).isAfter(new Date());
    }

    @Test
    void extractUsername_returnsTokenSubject() {
        String token = jwtService.generateAccessToken("john@example.com", "USER", 42L, "UABC123");

        assertThat(jwtService.extractUsername(token)).isEqualTo("john@example.com");
    }

    @Test
    void extractUsername_withInvalidToken_throwsUnauthorisedException() {
        assertThatThrownBy(() -> jwtService.extractUsername("not-a-token"))
                .isInstanceOf(UnauthorisedException.class)
                .hasMessage("Token decryption/verification failed");
    }

    @Test
    void parseAndValidate_withNullToken_throwsRuntimeException() {
        assertThatThrownBy(() -> jwtService.parseAndValidate(null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void parseAndValidate_withMalformedToken_throwsUnauthorisedException() {
        assertThatThrownBy(() -> jwtService.parseAndValidate("not-a-token"))
                .isInstanceOf(UnauthorisedException.class)
                .hasMessage("Token decryption/verification failed");
    }

    @Test
    void parseAndValidate_withTamperedToken_throwsUnauthorisedException() {
        String token = jwtService.generateAccessToken("john@example.com", "USER", 42L, "UABC123");
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "AAAA";

        assertThatThrownBy(() -> jwtService.parseAndValidate(tampered))
                .isInstanceOf(UnauthorisedException.class);
    }

    @Test
    void parseAndValidate_withWrongEncryptionKey_throwsUnauthorisedException() throws Exception {
        String token = jwtService.generateAccessToken("john@example.com", "USER", 42L, "UABC123");
        JwtService otherService = createJwtService(signingKeyPair, generateRsaKeyPair());

        assertThatThrownBy(() -> otherService.parseAndValidate(token))
                .isInstanceOf(UnauthorisedException.class)
                .hasMessage("Token decryption/verification failed");
    }

    @Test
    void parseAndValidate_withWrongSigningKey_throwsUnauthorisedException() throws Exception {
        String token = jwtService.generateAccessToken("john@example.com", "USER", 42L, "UABC123");
        JwtService otherService = createJwtService(generateRsaKeyPair(), encryptionKeyPair);

        assertThatThrownBy(() -> otherService.parseAndValidate(token))
                .isInstanceOf(UnauthorisedException.class)
                .hasMessage("JWT signature verification failed");
    }

    @Test
    void parseAndValidate_withExpiredToken_throwsUnauthorisedException() {
        ReflectionTestUtils.setField(jwtService, "ACCESS_TOKEN_TTL_MIN", -1L);
        String token = jwtService.generateAccessToken("john@example.com", "USER", 42L, "UABC123");

        assertThatThrownBy(() -> jwtService.parseAndValidate(token))
                .isInstanceOf(UnauthorisedException.class)
                .hasMessage("Token has expired");
    }

    @Test
    void parseAndValidate_withInvalidIssuer_throwsUnauthorisedException() {
        String token = jwtService.generateAccessToken("john@example.com", "USER", 42L, "UABC123");
        ReflectionTestUtils.setField(jwtService, "issuer", "another-issuer");

        assertThatThrownBy(() -> jwtService.parseAndValidate(token))
                .isInstanceOf(UnauthorisedException.class)
                .hasMessageContaining("Invalid issuer");
    }

    @Test
    void parseAndValidate_withInvalidTokenType_throwsUnauthorisedException() throws Exception {
        String token = encrypt(claimsWithType("refresh"));

        assertThatThrownBy(() -> jwtService.parseAndValidate(token))
                .isInstanceOf(UnauthorisedException.class)
                .hasMessage("Invalid token type");
    }

    @Test
    void parseAndValidate_whenPayloadIsNotNestedSignedJwt_throwsUnauthorisedException() throws Exception {
        JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
                .contentType("JWT")
                .keyID("enc-key-test")
                .build();
        JWEObject jwe = new JWEObject(header, new Payload("plain-text-payload"));
        jwe.encrypt(new RSAEncrypter((RSAPublicKey) encryptionKeyPair.getPublic()));

        assertThatThrownBy(() -> jwtService.parseAndValidate(jwe.serialize()))
                .isInstanceOf(UnauthorisedException.class)
                .hasMessage("Payload is not a nested signed JWT");
    }

    private JwtService createJwtService(KeyPair signing, KeyPair encryption) {
        JwtService service = new JwtService(
                (RSAPrivateKey) signing.getPrivate(),
                (RSAPublicKey) signing.getPublic(),
                (RSAPrivateKey) encryption.getPrivate(),
                (RSAPublicKey) encryption.getPublic()
        );
        ReflectionTestUtils.setField(service, "SIGNING_KEY_ID", "sign-key-test");
        ReflectionTestUtils.setField(service, "ENC_KEY_ID", "enc-key-test");
        ReflectionTestUtils.setField(service, "ACCESS_TOKEN_TTL_MIN", 15L);
        ReflectionTestUtils.setField(service, "REFRESH_TOKEN_TTL_MIN", 7L);
        ReflectionTestUtils.setField(service, "issuer", "demo-service");
        ReflectionTestUtils.setField(service, "audience", "demo-clients");
        return service;
    }

    private JWTClaimsSet claimsWithType(String type) {
        Instant now = Instant.now();
        return new JWTClaimsSet.Builder()
                .subject("john@example.com")
                .claim("roles", "USER")
                .claim("type", type)
                .claim("userId", 42L)
                .claim("userCode", "UABC123")
                .issuer("demo-service")
                .audience("demo-clients")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(900)))
                .jwtID("test-jwt-id")
                .build();
    }

    private String encrypt(JWTClaimsSet claims) throws Exception {
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID("sign-key-test")
                .build();
        SignedJWT signedJWT = new SignedJWT(jwsHeader, claims);
        signedJWT.sign(new RSASSASigner((RSAPrivateKey) signingKeyPair.getPrivate()));

        JWEHeader jweHeader = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
                .contentType("JWT")
                .keyID("enc-key-test")
                .build();
        JWEObject jwe = new JWEObject(jweHeader, new Payload(signedJWT));
        jwe.encrypt(new RSAEncrypter((RSAPublicKey) encryptionKeyPair.getPublic()));
        return jwe.serialize();
    }

    private KeyPair generateRsaKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }
}

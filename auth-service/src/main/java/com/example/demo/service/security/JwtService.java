package com.example.demo.service.security;

import com.example.commoncore.exception.UnauthorisedException;
import com.example.demo.exceptions.TokenGenerationException;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final RSAPrivateKey signingPrivateKey;
    private final RSAPublicKey signingPublicKey;
    private final RSAPrivateKey encryptionPrivateKey;
    private final RSAPublicKey encryptionPublicKey;

    public JwtService(@Qualifier("jwtSigningPrivateKey") RSAPrivateKey signingPrivateKey,
                      @Qualifier("jwtSigningPublicKey") RSAPublicKey signingPublicKey,
                      @Qualifier("jwtEncryptionPrivateKey") RSAPrivateKey encryptionPrivateKey,
                      @Qualifier("jwtEncryptionPublicKey") RSAPublicKey encryptionPublicKey) {
        this.signingPrivateKey = signingPrivateKey;
        this.signingPublicKey = signingPublicKey;
        this.encryptionPrivateKey = encryptionPrivateKey;
        this.encryptionPublicKey = encryptionPublicKey;

    }

    @Value("${security.jwt.signing.key-id}")
    private String SIGNING_KEY_ID;
    @Value("${security.jwt.encryption.key-id}")
    private String ENC_KEY_ID;
    @Value("${security.jwt.access-token.ttl-minutes}")
    long ACCESS_TOKEN_TTL_MIN;
    @Value("${security.jwt.issuer}")
    private String issuer;
    @Value("${security.jwt.audience}")
    private String audience;
    @Value("${security.jwt.refresh-token.ttl-days}")
    long REFRESH_TOKEN_TTL_MIN;

    public String generateAccessToken (String email, String roles, Long userId, String userCode){

        try {
            JWTClaimsSet claims = buildClaims(email,roles,userId,userCode);
            SignedJWT signed  = signClaims(claims);
            System.out.println(signed.serialize()); // here we get Actual Token
            return encryptSignedJwt(signed);
        } catch (JOSEException e) {
            throw new TokenGenerationException("Failed to generate access token");
        }
    }


    private JWTClaimsSet buildClaims(String email, String roles, Long userId, String userCode) {
        Instant now = Instant.now();

        return new JWTClaimsSet.Builder()
                .subject(email)
                .claim("roles",roles)
                .claim("type", "access")
                .claim("userId",userId)
                .claim("userCode",userCode)
                .issuer(issuer)
                .audience(audience)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(ACCESS_TOKEN_TTL_MIN, ChronoUnit.MINUTES)))
                .jwtID(UUID.randomUUID().toString())   // jti — useful for blacklisting
                .build();
    }


    private SignedJWT signClaims(JWTClaimsSet claims) throws JOSEException {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(SIGNING_KEY_ID)
                .build();

        SignedJWT signedJWT = new SignedJWT(header, claims);
        signedJWT.sign(new RSASSASigner(signingPrivateKey));
        return signedJWT;
    }

    private String encryptSignedJwt(SignedJWT signedJWT) throws JOSEException {
        JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
                .contentType("JWT")   // required for nested JWTs
                .keyID(ENC_KEY_ID)
                .build();

        JWEObject jwe = new JWEObject(header, new Payload(signedJWT));
        jwe.encrypt(new RSAEncrypter(encryptionPublicKey));
        return jwe.serialize();
    }

    private SignedJWT decryptToken(String token) throws ParseException, JOSEException {
        EncryptedJWT encryptedJWT = EncryptedJWT.parse(token);
        encryptedJWT.decrypt(new RSADecrypter(encryptionPrivateKey));

        SignedJWT signedJWT = encryptedJWT.getPayload().toSignedJWT();
        if (signedJWT == null) {
            throw new UnauthorisedException("Payload is not a nested signed JWT");
        }
        return signedJWT;
    }

    private void verifySignature(SignedJWT signedJWT) throws JOSEException {
        if (!signedJWT.verify(new RSASSAVerifier(signingPublicKey))) {
            throw new UnauthorisedException("JWT signature verification failed");
        }
    }

    private void validateClaims(JWTClaimsSet claims) {
        if (!issuer.equals(claims.getIssuer())) {
            throw new UnauthorisedException("Invalid issuer: " + claims.getIssuer());
        }
        if (claims.getExpirationTime() == null || claims.getExpirationTime().before(new Date())) {
            throw new UnauthorisedException("Token has expired");
        }
        if (!"access".equals(claims.getClaim("type"))) {
            throw new UnauthorisedException("Invalid token type");
        }
    }

    public JWTClaimsSet parseAndValidate(String token) {
        try {
            SignedJWT signedJWT = decryptToken(token);
            verifySignature(signedJWT);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            validateClaims(claims);
            return claims;
        } catch (ParseException | JOSEException e) {
            throw new UnauthorisedException("Token decryption/verification failed");
        }
    }


    public String extractUsername(String token) {
        return parseAndValidate(token).getSubject();
    }
}

package com.example.userservice.service.security;

import com.example.commoncore.exception.BadRequestException;
import com.example.commoncore.exception.UnauthorisedException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSADecrypter;
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
import java.util.Date;

@Service
public class JwtService {

    private final RSAPublicKey signingPublicKey;
    private final RSAPrivateKey encryptionPrivateKey;

    public JwtService(@Qualifier("jwtSigningPublicKey") RSAPublicKey signingPublicKey,
                      @Qualifier("jwtEncryptionPrivateKey") RSAPrivateKey encryptionPrivateKey) {
        this.signingPublicKey = signingPublicKey;
        this.encryptionPrivateKey = encryptionPrivateKey;

    }

    @Value("${security.jwt.issuer}")
    private String issuer;

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
            throw new BadRequestException("JWT signature verification failed");
        }
    }

    private void validateClaims(JWTClaimsSet claims) {
        if (!issuer.equals(claims.getIssuer())) {
            throw new BadRequestException("Invalid issuer: " + claims.getIssuer());
        }
        if (claims.getExpirationTime() == null || claims.getExpirationTime().before(new Date())) {
            throw new BadRequestException("Token has expired");
        }
        if (!"access".equals(claims.getClaim("type"))) {
            throw new BadRequestException("Invalid token type");
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
            throw new BadRequestException("Token decryption/verification failed");
        }
    }
}

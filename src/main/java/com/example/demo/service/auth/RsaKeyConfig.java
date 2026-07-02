package com.example.demo.service.auth;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class RsaKeyConfig {

    @Value("${security.jwt.signing.private-key}")
    private Resource privateKeyResource;

    @Value("${security.jwt.signing.public-key}")
    private Resource publicKeyResource;

    @Value("${security.jwt.encryption.private-key}")
    private Resource encryptionPrivateKeyResource;

    @Value("${security.jwt.encryption.public-key}")
    private Resource encryptionPublicKeyResource;

    @Bean("jwtSigningPrivateKey")
    public RSAPrivateKey jwtSigningPrivateKey() throws Exception{
        return loadPrivateKey(privateKeyResource);
    }

    @Bean("jwtSigningPublicKey")
    public RSAPublicKey jwtSigningPublicKey() throws Exception{
        return loadPublicKey(publicKeyResource);
    }

    @Bean("jwtEncryptionPrivateKey")
    public RSAPrivateKey jwtEncryptionPrivateKey() throws Exception{
        return loadPrivateKey(encryptionPrivateKeyResource);
    }

    @Bean("jwtEncryptionPublicKey")
    public RSAPublicKey jwtEncryptionPublicKey() throws Exception{
        return loadPublicKey(encryptionPublicKeyResource);
    }

    @Bean
    public RSAPrivateKey loadPrivateKey(Resource resource) throws Exception {
        String pem = readPem(resource);
        byte[] der = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    @Bean
    public RSAPublicKey loadPublicKey(Resource resource) throws Exception {
        String pem = readPem(resource);
        byte[] der = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private String readPem(Resource resource) throws IOException {
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return content
                .replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");
    }
}

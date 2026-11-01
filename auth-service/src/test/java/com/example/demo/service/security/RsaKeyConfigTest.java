package com.example.demo.service.security;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RsaKeyConfigTest {

    private final RsaKeyConfig rsaKeyConfig = new RsaKeyConfig();

    @Test
    void loadPrivateKey_fromClasspathPem_returnsRsaPrivateKey() throws Exception {
        RSAPrivateKey key = rsaKeyConfig.loadPrivateKey(new ClassPathResource("private.pem"));

        assertThat(key).isNotNull();
        assertThat(key.getAlgorithm()).isEqualTo("RSA");
    }

    @Test
    void loadPublicKey_fromClasspathPem_returnsRsaPublicKey() throws Exception {
        RSAPublicKey key = rsaKeyConfig.loadPublicKey(new ClassPathResource("public.pem"));

        assertThat(key).isNotNull();
        assertThat(key.getAlgorithm()).isEqualTo("RSA");
    }

    @Test
    void loadEncryptionKeys_fromClasspathPem_returnsRsaKeys() throws Exception {
        assertThat(rsaKeyConfig.loadPrivateKey(new ClassPathResource("encryption-private.pem"))).isNotNull();
        assertThat(rsaKeyConfig.loadPublicKey(new ClassPathResource("encryption-public.pem"))).isNotNull();
    }

    @Test
    void loadPrivateKey_withInvalidPem_throwsException() {
        ByteArrayResource invalidPem = new ByteArrayResource("not-a-private-key".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> rsaKeyConfig.loadPrivateKey(invalidPem))
                .isInstanceOf(Exception.class);
    }

    @Test
    void loadPublicKey_withInvalidPem_throwsException() {
        ByteArrayResource invalidPem = new ByteArrayResource("not-a-public-key".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> rsaKeyConfig.loadPublicKey(invalidPem))
                .isInstanceOf(Exception.class);
    }
}

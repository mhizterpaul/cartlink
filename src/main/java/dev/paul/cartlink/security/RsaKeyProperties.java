package dev.paul.cartlink.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@ConfigurationProperties(prefix = "rsa")
public class RsaKeyProperties {
    private String privateKeyLocation;
    private String publicKeyLocation;
    private final ResourceLoader resourceLoader;

    public RsaKeyProperties(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void setPrivateKey(String privateKeyLocation) {
        this.privateKeyLocation = privateKeyLocation;
    }

    public void setPublicKey(String publicKeyLocation) {
        this.publicKeyLocation = publicKeyLocation;
    }

    private String readKeyFromResource(String location) throws IOException {
        Resource resource = resourceLoader.getResource(location);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }

    @Bean
    public RSAPrivateKey privateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String privateKeyPEM = readKeyFromResource(privateKeyLocation)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    @Bean
    public RSAPublicKey publicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String publicKeyPEM = readKeyFromResource(publicKeyLocation)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }
}
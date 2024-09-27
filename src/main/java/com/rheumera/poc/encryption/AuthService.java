package com.rheumera.poc.encryption;

import com.rheumera.poc.encryption.AuthController.BiometricRequest;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    // This goes into db, mapped to specific user
    private static final Map<String, String> PUBLIC_KEYS = new HashMap<>();

    @SneakyThrows
    public boolean verifySignature(BiometricRequest request) {
        var publicKeyString = PUBLIC_KEYS.get(request.userId());
        if (publicKeyString == null) {
            throw new IllegalArgumentException("Public key not found for user " + request.userId());
        }
        var publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
        var keyFactory = KeyFactory.getInstance("RSA");
        var publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        var publicKey = keyFactory.generatePublic(publicKeySpec);
        var sign = Signature.getInstance("SHA256withRSA");
        sign.initVerify(publicKey);
        sign.update(request.message().getBytes());
        var signatureBytes = Base64.getDecoder().decode(request.signature());
        return sign.verify(signatureBytes);
    }

    public void addPublicKey(String publicKey, String userId) {
        PUBLIC_KEYS.put(userId, publicKey);
    }
}
package com.rheumera.poc.encryption;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PutMapping
    public void addPublicKey(@RequestBody AddPublicKey req) {
        authService.addPublicKey(req.publicKey(), req.userId());
    }

    @PostMapping
    public ResponseEntity<Boolean> verifySignature(@RequestBody BiometricRequest request) {
        var isVerified = authService.verifySignature(request);
        return ResponseEntity.status(isVerified ? 200 : 403).body(isVerified);
    }

    public static record BiometricRequest(String userId, String signature, String message) {}

    public static record AddPublicKey(String userId, String publicKey) {}
}
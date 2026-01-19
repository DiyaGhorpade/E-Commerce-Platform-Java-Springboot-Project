package com.example.auth_service.service;

import com.example.auth_service.entity.UserCredential;
import com.example.auth_service.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserCredentialRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public String saveUser(UserCredential credential) {
        // Encrypt password before saving
        credential.setPassword(passwordEncoder.encode(credential.getPassword()));
        repository.save(credential);
        return "user added to system";
    }

    public String generateToken(String username) {
        return jwtService.generateToken(username);
    }

    // --- THIS WAS MISSING ---
    public void validateToken(String token) {
        jwtService.validateToken(token);
    }
}
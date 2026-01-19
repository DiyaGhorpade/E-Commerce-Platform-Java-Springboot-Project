package com.example.auth_service.controller;

import com.example.auth_service.dto.AuthRequest;
import com.example.auth_service.entity.UserCredential;
import com.example.auth_service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService service;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public String addNewUser(@RequestBody UserCredential user) {
        return service.saveUser(user);
    }

    @PostMapping("/token")
    public String getToken(@RequestBody AuthRequest authRequest) {
        System.out.println("--- LOGIN ATTEMPT ---");
        System.out.println("Username received: " + authRequest.getUsername());
        System.out.println("Password received: " + authRequest.getPassword());

        try {
            // This is the line that throws the exception
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            if (authenticate.isAuthenticated()) {
                System.out.println("--- AUTH SUCCESS ---");
                return service.generateToken(authRequest.getUsername());
            } else {
                System.out.println("--- AUTH FAILED: Not Authenticated ---");
                throw new RuntimeException("invalid access");
            }
        } catch (Exception e) {
            // This will print the REAL error to your Docker logs
            System.out.println("--- AUTH EXCEPTION ---");
            e.printStackTrace(); 
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    @GetMapping("/validate")
    public String validateToken(@RequestParam("token") String token) {
        service.validateToken(token);
        return "Token is valid";
    }
}
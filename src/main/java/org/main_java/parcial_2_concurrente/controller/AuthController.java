package org.main_java.parcial_2_concurrente.controller;

import org.main_java.parcial_2_concurrente.model.userDTO.AuthResponseDTO;
import org.main_java.parcial_2_concurrente.model.userDTO.LoginRequestDTO;
import org.main_java.parcial_2_concurrente.model.userDTO.RegisterRequestDTO;
import org.main_java.parcial_2_concurrente.service.userService.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Login endpoint
    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponseDTO>> login(@RequestBody LoginRequestDTO loginRequest) {
        return authService.login(loginRequest);
    }

    // Register endpoint
    @PostMapping("/register")
    public Mono<ResponseEntity<AuthResponseDTO>> register(@RequestBody RegisterRequestDTO registerRequest) {
        return authService.register(registerRequest);
    }
}



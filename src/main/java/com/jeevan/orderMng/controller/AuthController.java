package com.jeevan.orderMng.controller;


import com.jeevan.orderMng.dto.AuthRequest;
import com.jeevan.orderMng.dto.AuthResponse;
import com.jeevan.orderMng.security.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @Operation(summary = "User login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return userService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token")
    public AuthResponse refreshToken(@RequestParam String refreshToken) {
        return userService.refreshToken(refreshToken);
    }
}
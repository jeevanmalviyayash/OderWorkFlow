package com.jeevan.orderMng.controller;

import com.jeevan.orderMng.entity.User;
import com.jeevan.orderMng.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "User Management", description = "Endpoints for users")
@RestController
@RequestMapping("/users")
public class UserController {


    @Autowired
    private IUserService userService;

    /**Get logged-in user's profile details. */
    @GetMapping("/me")
    @Operation(summary = "Get current logged-in user details")
    public User getCurrentUser(Authentication authentication) {
        String email = authentication.getName(); // Username is email in your system
        return userService.getUserByEmail(email);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

}

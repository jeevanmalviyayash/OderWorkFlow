package com.jeevan.orderMng.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class RoleChecker {

    public boolean hasRole(Authentication authentication, String[] roles) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return Arrays.stream(roles)
                .anyMatch(role -> authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + role)));
    }
}
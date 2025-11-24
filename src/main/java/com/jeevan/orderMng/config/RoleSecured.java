package com.jeevan.orderMng.config;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("@roleChecker.hasRole(authentication, #roles)")
public @interface RoleSecured {
    String[] roles();
}
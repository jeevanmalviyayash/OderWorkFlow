package com.jeevan.orderMng.controller;

import com.jeevan.orderMng.entity.AuditLog;
import com.jeevan.orderMng.service.IAuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Audit Logs", description = "Endpoints to view audit logs")
@RestController
@RequestMapping("/audit")
public class AuditLogController {

    @Autowired
    private IAuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Get audit logs (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogService.getAuditLogs(pageable);
    }
}

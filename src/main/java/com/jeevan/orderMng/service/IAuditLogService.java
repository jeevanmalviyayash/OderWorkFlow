package com.jeevan.orderMng.service;

import com.jeevan.orderMng.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IAuditLogService {
    Page<AuditLog> getAuditLogs(Pageable pageable);
}

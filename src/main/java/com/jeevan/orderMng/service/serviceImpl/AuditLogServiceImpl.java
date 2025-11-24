package com.jeevan.orderMng.service.serviceImpl;

import com.jeevan.orderMng.entity.AuditLog;
import com.jeevan.orderMng.repository.AuditLogRepository;
import com.jeevan.orderMng.service.IAuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl implements IAuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }
}


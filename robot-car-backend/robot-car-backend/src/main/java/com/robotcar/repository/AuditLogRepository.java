package com.robotcar.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.robotcar.entity.AuditLog;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

}
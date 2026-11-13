package com.example.productservice.repositories.admin;

import com.example.productservice.entities.admin.AdminSettingAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminSettingAuditRepository extends JpaRepository<AdminSettingAudit, Long> {

    List<AdminSettingAudit>
    findBySettingKeyOrderByChangedAtDesc(String settingKey);

}

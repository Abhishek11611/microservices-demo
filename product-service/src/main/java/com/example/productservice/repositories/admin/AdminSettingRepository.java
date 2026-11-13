package com.example.productservice.repositories.admin;

import com.example.productservice.entities.admin.AdminSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminSettingRepository extends JpaRepository<AdminSetting, String> {

    List<AdminSetting> findByModule(String module);

}

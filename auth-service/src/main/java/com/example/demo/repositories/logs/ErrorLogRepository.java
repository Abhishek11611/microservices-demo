package com.example.demo.repositories.logs;

import com.example.demo.entities.logs.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
}

package com.hrmanagement.repository;

import com.hrmanagement.entity.PayrollConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayrollConfigRepository extends JpaRepository<PayrollConfig, Long> {
    Optional<PayrollConfig> findByConfigKey(String configKey);
}

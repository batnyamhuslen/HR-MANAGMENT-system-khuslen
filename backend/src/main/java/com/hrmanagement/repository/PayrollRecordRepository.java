package com.hrmanagement.repository;

import com.hrmanagement.entity.PayrollRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, Long> {

    Optional<PayrollRecord> findByEmployeeIdAndPayPeriodYearAndPayPeriodMonth(
            Long employeeId, int payPeriodYear, int payPeriodMonth);

    Page<PayrollRecord> findByPayPeriodYearAndPayPeriodMonth(int payPeriodYear, int payPeriodMonth, Pageable pageable);

    List<PayrollRecord> findByEmployeeIdOrderByPayPeriodYearDescPayPeriodMonthDesc(Long employeeId);
}

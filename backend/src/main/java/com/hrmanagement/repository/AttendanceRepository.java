package com.hrmanagement.repository;

import com.hrmanagement.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.hrmanagement.enums.AttendanceStatus;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    List<Attendance> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate from, LocalDate to);

    @Query("SELECT a FROM Attendance a WHERE a.date = :date")
    List<Attendance> findByDate(@Param("date") LocalDate date);

    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId " +
           "AND a.date BETWEEN :startDate AND :endDate")
    List<Attendance> findMonthlyByEmployee(@Param("employeeId") Long employeeId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.date = :date AND CAST(a.status AS text) IN :statuses")
    long countByDateAndStatusIn(@Param("date") LocalDate date, @Param("statuses") List<String> statuses);
}

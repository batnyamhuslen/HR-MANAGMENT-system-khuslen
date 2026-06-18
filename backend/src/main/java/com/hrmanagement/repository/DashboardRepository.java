package com.hrmanagement.repository;

import com.hrmanagement.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface DashboardRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT COUNT(e) FROM Employee e WHERE CAST(e.status AS text) <> 'TERMINATED'")
    long countActiveEmployees();

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.date = :date AND CAST(a.status AS text) IN ('PRESENT', 'LATE')")
    long countTodayPresentOrLate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(DISTINCT lr.employee.id) FROM LeaveRequest lr WHERE lr.status = 'APPROVED' AND :date BETWEEN lr.startDate AND lr.endDate")
    long countDistinctEmployeesOnLeaveToday(@Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.id = :employeeId AND a.date BETWEEN :startDate AND :endDate AND CAST(a.status AS text) IN ('PRESENT', 'LATE')")
    long countEmployeeAttendanceThisMonth(@Param("employeeId") Long employeeId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.id = :employeeId AND a.date BETWEEN :startDate AND :endDate")
    long countEmployeeTotalAttendanceDays(@Param("employeeId") Long employeeId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(lb.used), 0) FROM LeaveBalance lb WHERE lb.employee.id = :employeeId AND lb.year = :year")
    int sumEmployeeUsedLeaveDays(@Param("employeeId") Long employeeId, @Param("year") int year);

    @Query("SELECT COALESCE(SUM(lb.remaining), 0) FROM LeaveBalance lb WHERE lb.employee.id = :employeeId AND lb.year = :year")
    int sumEmployeeRemainingLeaveDays(@Param("employeeId") Long employeeId, @Param("year") int year);
}

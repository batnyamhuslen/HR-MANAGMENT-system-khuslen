package com.hrmanagement.repository;

import com.hrmanagement.entity.LeaveRequest;
import com.hrmanagement.enums.LeaveRequestStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeId(Long employeeId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = 'PENDING' ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findPendingRequests(Pageable pageable);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId " +
           "AND lr.status = 'APPROVED' " +
           "AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    List<LeaveRequest> findOverlappingApprovedLeaves(@Param("employeeId") Long employeeId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.employee.id = :employeeId " +
           "AND lr.status = 'APPROVED' " +
           "AND :date BETWEEN lr.startDate AND lr.endDate")
    long countEmployeesOnLeaveByDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE CAST(lr.status AS text) = :status " +
           "AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    long countByStatusAndDateRange(@Param("status") String status,
                                   @Param("endDate") LocalDate endDate,
                                   @Param("startDate") LocalDate startDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId " +
           "AND lr.status = 'APPROVED' " +
           "AND lr.leaveType.name = :leaveTypeName " +
           "AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    List<LeaveRequest> findOverlappingApprovedLeaveByType(@Param("employeeId") Long employeeId,
                                                           @Param("leaveTypeName") String leaveTypeName,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);
}

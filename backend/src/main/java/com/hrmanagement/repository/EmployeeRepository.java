package com.hrmanagement.repository;

import com.hrmanagement.entity.Employee;
import com.hrmanagement.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(COALESCE(e.firstName, '') || ' ' || COALESCE(e.lastName, '') || ' ' || COALESCE(e.email, '')) " +
           "LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%')) " +
           "AND (:departmentId IS NULL OR e.department.id = :departmentId) " +
           "AND (:status IS NULL OR CAST(e.status AS text) = :status)")
    Page<Employee> searchEmployees(@Param("search") String search,
                                   @Param("departmentId") Long departmentId,
                                   @Param("status") String status,
                                   Pageable pageable);

    @Query("SELECT COUNT(e) FROM Employee e WHERE CAST(e.status AS text) <> :status")
    long countByStatusNot(@Param("status") String status);
}

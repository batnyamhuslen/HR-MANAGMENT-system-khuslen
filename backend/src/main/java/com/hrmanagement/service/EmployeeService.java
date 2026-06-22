package com.hrmanagement.service;

import com.hrmanagement.dto.EmployeeCreateRequest;
import com.hrmanagement.dto.EmployeeDto;
import com.hrmanagement.dto.EmployeeUpdateRequest;
import com.hrmanagement.entity.Department;
import com.hrmanagement.entity.Employee;
import com.hrmanagement.entity.User;
import com.hrmanagement.enums.EmployeeStatus;
import com.hrmanagement.enums.NotificationType;
import com.hrmanagement.enums.UserRole;
import com.hrmanagement.repository.DepartmentRepository;
import com.hrmanagement.repository.EmployeeRepository;
import com.hrmanagement.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           UserRepository userRepository,
                           NotificationService notificationService) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public Page<EmployeeDto> search(String search, Long departmentId, String status, Pageable pageable) {
        if (search != null && search.isBlank()) search = null;
        if (status != null && status.isBlank()) status = null;
        return employeeRepository.searchEmployees(search, departmentId, status, pageable)
                .map(this::toDto);
    }

    public EmployeeDto getById(Long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
        return toDto(emp);
    }

    public EmployeeDto create(EmployeeCreateRequest request) {
        Employee emp = new Employee();
        emp.setEmployeeCode(request.getEmployeeCode());
        emp.setFirstName(request.getFirstName());
        emp.setLastName(request.getLastName());
        emp.setEmail(request.getEmail());
        emp.setPhone(request.getPhone());
        emp.setDateOfBirth(request.getDateOfBirth());
        emp.setHireDate(request.getHireDate());
        emp.setPosition(request.getPosition());
        emp.setSalary(request.getSalary());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
            emp.setDepartment(dept);
        }

        if (request.getStatus() != null) {
            emp.setStatus(EmployeeStatus.valueOf(request.getStatus().toUpperCase()));
        }

        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new IllegalArgumentException("Manager not found"));
            emp.setManager(manager);
        }

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            emp.setUser(user);
        }

        Employee saved = employeeRepository.save(emp);

        List<User> adminsAndHr = userRepository.findByRoleIn(List.of(UserRole.ADMIN, UserRole.HR));
        List<Long> recipientIds = new ArrayList<>();
        for (User u : adminsAndHr) {
            recipientIds.add(u.getId());
        }
        if (!recipientIds.isEmpty()) {
            String employeeName = saved.getFirstName() + " " + saved.getLastName();
            String deptName = saved.getDepartment() != null ? saved.getDepartment().getName() : "";
            String message = employeeName + " " + deptName + " хэлтэст элссэн";
            notificationService.createNotificationForUsers(recipientIds, NotificationType.EMPLOYEE_HIRED,
                    "Шинэ ажилтан", message,
                    "/employees/" + saved.getId(), saved.getId());
        }

        return toDto(saved);
    }

    public EmployeeDto update(Long id, EmployeeUpdateRequest request) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
        EmployeeStatus oldStatus = emp.getStatus();
        emp.setFirstName(request.getFirstName());
        emp.setLastName(request.getLastName());
        emp.setEmail(request.getEmail());
        emp.setPhone(request.getPhone());
        emp.setDateOfBirth(request.getDateOfBirth());
        if (request.getHireDate() != null) emp.setHireDate(request.getHireDate());
        emp.setPosition(request.getPosition());
        emp.setSalary(request.getSalary());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
            emp.setDepartment(dept);
        } else {
            emp.setDepartment(null);
        }

        if (request.getStatus() != null) {
            emp.setStatus(EmployeeStatus.valueOf(request.getStatus().toUpperCase()));
        }

        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new IllegalArgumentException("Manager not found"));
            emp.setManager(manager);
        } else {
            emp.setManager(null);
        }

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            emp.setUser(user);
        } else {
            emp.setUser(null);
        }

        Employee saved = employeeRepository.save(emp);

        if (oldStatus != EmployeeStatus.TERMINATED && saved.getStatus() == EmployeeStatus.TERMINATED) {
            List<User> adminsAndHr = userRepository.findByRoleIn(List.of(UserRole.ADMIN, UserRole.HR));
            List<Long> recipientIds = new ArrayList<>();
            for (User u : adminsAndHr) {
                recipientIds.add(u.getId());
            }
            if (!recipientIds.isEmpty()) {
                String employeeName = saved.getFirstName() + " " + saved.getLastName();
                String message = employeeName + "-н ажил хасагдлаа";
                notificationService.createNotificationForUsers(recipientIds, NotificationType.EMPLOYEE_TERMINATED,
                        "Ажилтан халагдсан", message,
                        "/employees/" + saved.getId(), saved.getId());
            }
        }

        return toDto(saved);
    }

    public void delete(Long id) {
        employeeRepository.deleteById(id);
    }

    private EmployeeDto toDto(Employee emp) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(emp.getId());
        dto.setEmployeeCode(emp.getEmployeeCode());
        dto.setFirstName(emp.getFirstName());
        dto.setLastName(emp.getLastName());
        dto.setEmail(emp.getEmail());
        dto.setPhone(emp.getPhone());
        dto.setDateOfBirth(emp.getDateOfBirth());
        dto.setHireDate(emp.getHireDate());
        dto.setPosition(emp.getPosition());
        dto.setSalary(emp.getSalary());
        dto.setStatus(emp.getStatus().name());
        if (emp.getDepartment() != null) {
            dto.setDepartmentId(emp.getDepartment().getId());
            dto.setDepartmentName(emp.getDepartment().getName());
        }
        if (emp.getManager() != null) {
            dto.setManagerId(emp.getManager().getId());
            dto.setManagerName(emp.getManager().getFirstName() + " " + emp.getManager().getLastName());
        }
        if (emp.getUser() != null) {
            dto.setUserId(emp.getUser().getId());
        }
        return dto;
    }
}

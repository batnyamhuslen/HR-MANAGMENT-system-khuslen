package com.hrmanagement.controller;

import com.hrmanagement.dto.EmployeeCreateRequest;
import com.hrmanagement.dto.EmployeeDto;
import com.hrmanagement.dto.EmployeeUpdateRequest;
import com.hrmanagement.dto.EmployeeWithUserRequest;
import com.hrmanagement.entity.User;
import com.hrmanagement.enums.UserRole;
import com.hrmanagement.repository.UserRepository;
import com.hrmanagement.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeController(EmployeeService employeeService,
                              UserRepository userRepository,
                              PasswordEncoder passwordEncoder) {
        this.employeeService = employeeService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeDto>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(employeeService.search(search, departmentId, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    @PostMapping("/with-user")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<EmployeeDto> createWithUser(@Valid @RequestBody EmployeeWithUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Хэрэглэгчийн нэр бүртгэгдсэн байна");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Имэйл бүртгэгдсэн байна");
        }

        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getEmail(),
                UserRole.EMPLOYEE
        );
        user = userRepository.save(user);

        EmployeeCreateRequest empReq = new EmployeeCreateRequest();
        empReq.setEmployeeCode(request.getEmployeeCode());
        empReq.setFirstName(request.getFirstName());
        empReq.setLastName(request.getLastName());
        empReq.setEmail(request.getEmail());
        empReq.setPhone(request.getPhone());
        empReq.setDateOfBirth(request.getDateOfBirth());
        empReq.setHireDate(request.getHireDate());
        empReq.setDepartmentId(request.getDepartmentId());
        empReq.setPosition(request.getPosition());
        empReq.setSalary(request.getSalary());
        empReq.setUserId(user.getId());
        empReq.setStatus("ACTIVE");

        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(empReq));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateRequest request) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

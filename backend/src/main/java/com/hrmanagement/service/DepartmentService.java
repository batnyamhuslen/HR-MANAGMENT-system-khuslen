package com.hrmanagement.service;

import com.hrmanagement.dto.DepartmentDto;
import com.hrmanagement.entity.Department;
import com.hrmanagement.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<DepartmentDto> getAll() {
        return departmentRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public DepartmentDto getById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + id));
        return toDto(dept);
    }

    public DepartmentDto create(DepartmentDto dto) {
        Department dept = new Department(dto.getName(), dto.getDescription());
        return toDto(departmentRepository.save(dept));
    }

    public DepartmentDto update(Long id, DepartmentDto dto) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + id));
        dept.setName(dto.getName());
        dept.setDescription(dto.getDescription());
        return toDto(departmentRepository.save(dept));
    }

    public void delete(Long id) {
        departmentRepository.deleteById(id);
    }

    private DepartmentDto toDto(Department dept) {
        return new DepartmentDto(dept.getId(), dept.getName(), dept.getDescription());
    }
}

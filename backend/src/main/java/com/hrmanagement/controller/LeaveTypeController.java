package com.hrmanagement.controller;

import com.hrmanagement.entity.LeaveType;
import com.hrmanagement.repository.LeaveTypeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leave-types")
public class LeaveTypeController {

    private final LeaveTypeRepository leaveTypeRepository;

    public LeaveTypeController(LeaveTypeRepository leaveTypeRepository) {
        this.leaveTypeRepository = leaveTypeRepository;
    }

    @GetMapping
    public ResponseEntity<List<LeaveType>> getAll() {
        return ResponseEntity.ok(leaveTypeRepository.findAll());
    }
}

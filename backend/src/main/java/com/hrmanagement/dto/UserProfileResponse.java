package com.hrmanagement.dto;

import java.util.Map;

public class UserProfileResponse {

    private static final Map<String, String> ROLE_LABELS = Map.of(
        "ADMIN", "Админ",
        "HR", "HR Менежер",
        "MANAGER", "Менежер",
        "EMPLOYEE", "Ажилтан"
    );

    private String fullName;
    private String initials;
    private String roleLabel;
    private String role;

    public UserProfileResponse() {}

    public UserProfileResponse(String fullName, String role) {
        this.fullName = fullName;
        if (fullName != null && !fullName.isBlank()) {
            String[] parts = fullName.split(" ");
            if (parts.length >= 2) {
                this.initials = String.valueOf(parts[0].charAt(0)) + parts[1].charAt(0);
            } else {
                this.initials = String.valueOf(parts[0].charAt(0));
            }
        } else {
            this.initials = "";
        }
        this.roleLabel = ROLE_LABELS.getOrDefault(role, role);
        this.role = role;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) {
        this.fullName = fullName;
        if (fullName != null && !fullName.isBlank()) {
            String[] parts = fullName.split(" ");
            if (parts.length >= 2) {
                this.initials = String.valueOf(parts[0].charAt(0)) + parts[1].charAt(0);
            } else {
                this.initials = String.valueOf(parts[0].charAt(0));
            }
        }
    }
    public String getInitials() { return initials; }
    public void setInitials(String initials) { this.initials = initials; }
    public String getRoleLabel() { return roleLabel; }
    public void setRoleLabel(String roleLabel) { this.roleLabel = roleLabel; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

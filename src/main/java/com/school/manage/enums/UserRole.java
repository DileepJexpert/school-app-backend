package com.school.manage.enums;

/**
 * Extensible role definitions for the school management platform.
 *
 * Current roles:
 *   SUPER_ADMIN        – Platform-level admin (manages all schools on the platform)
 *   SCHOOL_ADMIN       – Full access within a single school
 *   TEACHER            – Manages attendance, results, and timetable for assigned classes
 *   ACCOUNTANT         – Manages fees, expenses, and financial reports
 *   TRANSPORT_MANAGER  – Manages buses, routes, and student transport assignments
 *   STUDENT            – Read-only access to own records
 *   PARENT             – Read-only access to linked child's records
 *
 * To add a new role (e.g., LIBRARIAN):
 *   1. Add the constant here
 *   2. Add permission mapping in AuthService.getDefaultPermissions()
 *   3. Add @PreAuthorize rules to relevant controllers
 *   4. Update Flutter UserRole constants and menu visibility
 */
public enum UserRole {
    SUPER_ADMIN,
    SCHOOL_ADMIN,
    TEACHER,
    ACCOUNTANT,
    TRANSPORT_MANAGER,
    STUDENT,
    PARENT
}

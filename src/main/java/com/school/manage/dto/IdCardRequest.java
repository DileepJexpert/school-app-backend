package com.school.manage.dto;

import lombok.Data;

@Data
public class IdCardRequest {
    private String studentId;
    private String type; // STUDENT, STAFF
}

package com.school.manage.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Response returned after a public admission enquiry is submitted.
 */
@Data
@Builder
public class PublicAdmissionResponse {
    private boolean success;
    private String message;
    private String enquiryNumber; // e.g. "ENQ-1001"
    private String studentName;
}

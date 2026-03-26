package com.school.manage.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CertificateRequestDto {

    private String studentId;

    // TRANSFER, BONAFIDE, CHARACTER, STUDY, ID_CARD
    private String certificateType;

    private String reason;

    /** Additional fields like leavingDate, conductRemark, etc. */
    private Map<String, String> additionalFields;
}

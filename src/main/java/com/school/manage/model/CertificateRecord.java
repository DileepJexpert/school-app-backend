package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "certificate_records")
public class CertificateRecord {

    @Id
    private String id;

    private String studentId;
    private String studentName;
    private String className;
    private String academicYear;

    // TRANSFER, BONAFIDE, CHARACTER, STUDY, ID_CARD
    private String certificateType;

    private String serialNumber;
    private String reason;

    /** Additional fields specific to certificate type (e.g., TC leaving date) */
    private Map<String, String> additionalFields;

    private String generatedBy;
    private LocalDateTime generatedAt = LocalDateTime.now();
}

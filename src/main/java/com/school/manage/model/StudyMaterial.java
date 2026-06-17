package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "study_materials")
public class StudyMaterial {
    @Id
    private String id;
    private String title;
    private String description;
    private String subject;
    private String className;
    private String materialType;
    private String contentUrl;
    private String fileData;
    private String fileName;
    private String fileType;
    private long fileSize;
    private String academicYear;
    private String chapter;
    private int sortOrder;
    private String uploadedBy;
    private boolean published = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

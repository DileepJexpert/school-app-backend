package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "tutorial_videos")
public class TutorialVideo {

    @Id
    private String id;

    private String title;
    private String description;
    private String subject;
    private String className;
    private String chapter;

    private String teacherId;
    private String teacherName;

    // GridFS reference
    private String gridFsFileId;
    private String fileName;
    private String contentType;
    private long fileSize;

    private String status = "ACTIVE"; // ACTIVE, ARCHIVED
    private LocalDateTime createdAt;
}

package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "gallery")
public class Gallery {
    @Id
    private String id;
    private String title;
    private String description;
    private String category;          // EVENT, ACADEMIC, SPORTS, CULTURAL, CAMPUS, OTHER
    private String eventId;           // optional link to event
    private List<GalleryImage> images;
    private String coverImageUrl;     // first image or selected cover
    private String academicYear;
    private LocalDate date;
    private String uploadedBy;
    private boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class GalleryImage {
        private String id;            // UUID
        private String url;           // image URL or base64
        private String caption;
        private int sortOrder;
    }
}

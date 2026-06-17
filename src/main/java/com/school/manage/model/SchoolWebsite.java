package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "school_website")
public class SchoolWebsite {

    @Id
    private String id;

    @Indexed(unique = true)
    private String tenantId;

    // ── Identity ──
    private String schoolName;
    private String shortName;
    private String tagline;
    private String accreditation;

    // ── Contact ──
    private String phone;
    private String email;
    private String address;
    private String officeHours;

    // ── Theme ──
    private String primaryColor;
    private String secondaryColor;
    private String logoUrl;

    // ── Hero Section ──
    private String heroImageUrl;
    private String marqueeText;

    // ── Principal ──
    private String principalName;
    private String principalTitle;
    private String principalMessage;
    private String principalImageUrl;

    // ── About ──
    private String mission;
    private String vision;

    // ── Dynamic sections — Lists of maps for flexibility ──
    private List<Map<String, String>> stats;
    private List<Map<String, String>> coreValues;
    private List<Map<String, String>> achievements;
    private List<Map<String, String>> testimonials;
    private List<Map<String, String>> events;
    private List<Map<String, String>> notices;
    private List<Map<String, String>> academicLevels;
    private List<Map<String, String>> coCurriculars;
    private List<Map<String, String>> feeStructure;
    private List<Map<String, String>> admissionSteps;
    private List<Map<String, String>> importantDates;
    private List<Map<String, String>> galleryImages;
    private List<Map<String, String>> transportZones;
    private List<String> transportFeatures;
    private List<Map<String, String>> timeline;

    // ── Social ──
    private Map<String, String> socialLinks;

    private LocalDateTime updatedAt;
}

package com.school.manage.controller;

import com.school.manage.dto.PublicAdmissionRequest;
import com.school.manage.dto.PublicAdmissionResponse;
import com.school.manage.model.*;
import com.school.manage.service.StudentService;
import com.school.manage.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

/**
 * Public (unauthenticated) endpoints for the online admission form.
 * Parents visit the school's website and submit an admission enquiry
 * without needing to log in.
 *
 * Security: /public/** is already permitted in SecurityConfig.
 */
@Slf4j
@RestController
@RequestMapping("/public/admissions")
@CrossOrigin(origins = "*")
public class PublicAdmissionController {

    private final StudentService studentService;
    private final MongoTemplate platformMongoTemplate;

    private static final List<String> AVAILABLE_CLASSES = List.of(
            "Nursery", "LKG", "UKG",
            "Class 1", "Class 2", "Class 3", "Class 4",
            "Class 5", "Class 6", "Class 7", "Class 8",
            "Class 9", "Class 10", "Class 11", "Class 12"
    );

    public PublicAdmissionController(StudentService studentService,
                                     @Qualifier("platformMongoTemplate") MongoTemplate platformMongoTemplate) {
        this.studentService = studentService;
        this.platformMongoTemplate = platformMongoTemplate;
    }

    /**
     * Submit an online admission enquiry for a school.
     *
     * @param tenantId the school's tenant identifier
     * @param request  the admission form data filled in by a parent
     * @return success response with the generated enquiry number
     */
    @PostMapping("/{tenantId}")
    public ResponseEntity<PublicAdmissionResponse> submitPublicAdmission(
            @PathVariable String tenantId,
            @RequestBody PublicAdmissionRequest request) {

        // 1. Validate that the school (tenant) exists and is active
        validateTenant(tenantId);

        // 2. Validate required fields
        validateRequest(request);

        String prev = TenantContext.getTenant();
        try {
            // 3. Set tenant context so the repository writes to the correct DB
            TenantContext.setTenant(tenantId);

            // 4. Map DTO -> Student model
            Student student = mapToStudent(request);

            // 5. Save as enquiry (status = ENQUIRY, auto-generates ENQ-XXXX number)
            Student saved = studentService.saveEnquiry(student);

            log.info("[PublicAdmission] Enquiry '{}' submitted for tenant '{}' — student '{}'",
                    saved.getAdmissionNumber(), tenantId, saved.getFullName());

            // 6. Return success response
            return ResponseEntity.ok(PublicAdmissionResponse.builder()
                    .success(true)
                    .message("Admission enquiry submitted successfully. " +
                             "Please note your enquiry number for future reference.")
                    .enquiryNumber(saved.getAdmissionNumber())
                    .studentName(saved.getFullName())
                    .build());

        } finally {
            // 7. Restore / clear tenant context
            if (prev != null) {
                TenantContext.setTenant(prev);
            } else {
                TenantContext.clear();
            }
        }
    }

    /**
     * Returns the list of class options for the admission form dropdown.
     *
     * @param tenantId the school's tenant identifier
     * @return list of available class names
     */
    @GetMapping("/{tenantId}/classes")
    public ResponseEntity<List<String>> getAvailableClasses(@PathVariable String tenantId) {
        validateTenant(tenantId);
        return ResponseEntity.ok(AVAILABLE_CLASSES);
    }

    // -----------------------------------------------------------------------
    //  Private helpers
    // -----------------------------------------------------------------------

    /**
     * Checks that the tenantId corresponds to an active school in platform_db.
     */
    private void validateTenant(String tenantId) {
        School school = platformMongoTemplate.findOne(
                Query.query(Criteria.where("tenantId").is(tenantId).and("active").is(true)),
                School.class);
        if (school == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "School not found or inactive for tenant: " + tenantId);
        }
    }

    /**
     * Validates the required fields in the admission request.
     */
    private void validateRequest(PublicAdmissionRequest request) {
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Student full name is required");
        }
        if (request.getClassForAdmission() == null || request.getClassForAdmission().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Class for admission is required");
        }
        boolean hasFatherMobile = request.getFatherMobile() != null && !request.getFatherMobile().isBlank();
        boolean hasMotherMobile = request.getMotherMobile() != null && !request.getMotherMobile().isBlank();
        if (!hasFatherMobile && !hasMotherMobile) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "At least one parent mobile number (father or mother) is required");
        }
    }

    /**
     * Maps the public admission DTO to the Student domain model.
     */
    private Student mapToStudent(PublicAdmissionRequest request) {
        Student student = new Student();

        // Student info
        student.setFullName(request.getFullName().trim());
        if (request.getDateOfBirth() != null && !request.getDateOfBirth().isBlank()) {
            student.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
        }
        student.setGender(request.getGender());
        student.setBloodGroup(request.getBloodGroup());
        student.setNationality(request.getNationality());
        student.setReligion(request.getReligion());
        student.setMotherTongue(request.getMotherTongue());
        student.setAadharNumber(request.getAadharNumber());
        student.setClassForAdmission(request.getClassForAdmission());

        // Academic year — auto-calculate if not provided
        if (request.getAcademicYear() != null && !request.getAcademicYear().isBlank()) {
            student.setAcademicYear(request.getAcademicYear());
        } else {
            student.setAcademicYear(computeCurrentAcademicYear());
        }

        // Parent details
        ParentDetails parentDetails = new ParentDetails();
        parentDetails.setFatherName(request.getFatherName());
        parentDetails.setFatherOccupation(request.getFatherOccupation());
        parentDetails.setFatherMobile(request.getFatherMobile());
        parentDetails.setFatherEmail(request.getFatherEmail());
        parentDetails.setMotherName(request.getMotherName());
        parentDetails.setMotherOccupation(request.getMotherOccupation());
        parentDetails.setMotherMobile(request.getMotherMobile());
        parentDetails.setMotherEmail(request.getMotherEmail());
        student.setParentDetails(parentDetails);

        // Contact details
        ContactDetails contactDetails = new ContactDetails();
        if (request.getAddress() != null) {
            contactDetails.setPermanentAddress(request.getAddress());
            contactDetails.setCorrespondenceAddress(request.getAddress());
        }
        contactDetails.setPrimaryContactNumber(request.getPrimaryContactNumber());
        student.setContactDetails(contactDetails);

        // Previous school details (optional)
        if (request.getPreviousSchoolName() != null && !request.getPreviousSchoolName().isBlank()) {
            PreviousSchoolDetails prevSchool = new PreviousSchoolDetails();
            prevSchool.setSchoolName(request.getPreviousSchoolName());
            prevSchool.setLastClass(request.getPreviousClass());
            prevSchool.setBoard(request.getPreviousBoard());
            student.setPreviousSchoolDetails(prevSchool);
        }

        return student;
    }

    /**
     * Computes the current Indian academic year string.
     * Indian academic year runs April–March.
     * If the current month is April or later: "currentYear-nextYear"
     * Otherwise: "lastYear-currentYear"
     */
    private String computeCurrentAcademicYear() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        if (today.getMonth().getValue() >= Month.APRIL.getValue()) {
            return year + "-" + (year + 1);
        } else {
            return (year - 1) + "-" + year;
        }
    }
}

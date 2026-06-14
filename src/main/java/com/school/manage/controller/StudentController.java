package com.school.manage.controller;

import com.school.manage.model.ContactDetails;
import com.school.manage.model.ParentDetails;
import com.school.manage.model.Student;
import com.school.manage.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<Student> admitNewStudent(@RequestBody Student student) {
        log.info("[StudentController] POST /api/students/add — admitting student: name='{}'", student.getFullName());
        Student admittedStudent = studentService.admitStudent(student);
        log.info("[StudentController] Student admitted: id='{}', admissionNo='{}'",
                admittedStudent.getId(), admittedStudent.getAdmissionNumber());
        return new ResponseEntity<>(admittedStudent, HttpStatus.CREATED);
    }

    @PostMapping("/enquiry")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<Student> saveEnquiry(@RequestBody Student student) {
        log.info("[StudentController] POST /api/students/enquiry — name='{}'", student.getFullName());
        Student saved = studentService.saveEnquiry(student);
        log.info("[StudentController] Enquiry saved: id='{}', enquiryNo='{}'", saved.getId(), saved.getAdmissionNumber());
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','ACCOUNTANT','TRANSPORT_MANAGER')")
    public ResponseEntity<List<Student>> getAllStudents(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String academicYear) {
        log.debug("[StudentController] GET /api/students — className='{}', academicYear='{}'", className, academicYear);
        List<Student> students = studentService.getAllStudents();
        if (className != null && !className.isBlank()) {
            students = students.stream()
                    .filter(s -> className.equals(s.getClassForAdmission()))
                    .toList();
        }
        if (academicYear != null && !academicYear.isBlank()) {
            students = students.stream()
                    .filter(s -> academicYear.equals(s.getAcademicYear()))
                    .toList();
        }
        log.debug("[StudentController] Returning {} student(s)", students.size());
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','ACCOUNTANT','TRANSPORT_MANAGER','STUDENT','PARENT')")
    public ResponseEntity<Student> getStudentById(@PathVariable String id) {
        log.debug("[StudentController] GET /api/students/{}", id);
        return studentService.getStudentById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("[StudentController] Student not found: id='{}'", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','ACCOUNTANT','TRANSPORT_MANAGER')")
    public ResponseEntity<List<Student>> searchStudents(@RequestParam String name) {
        log.debug("[StudentController] GET /api/students/search?name='{}'", name);
        List<Student> students = studentService.searchStudents(name);
        log.debug("[StudentController] Search '{}' returned {} result(s)", name, students.size());
        return ResponseEntity.ok(students);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<Student> updateStudent(@PathVariable String id, @RequestBody Student student) {
        log.info("[StudentController] PUT /api/students/{}", id);
        try {
            Student updatedStudent = studentService.updateStudent(id, student);
            log.info("[StudentController] Student updated: id='{}'", id);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            log.warn("[StudentController] Update FAILED for student id='{}': {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable String id) {
        log.info("[StudentController] DELETE /api/students/{}", id);
        studentService.deleteStudent(id);
        log.info("[StudentController] Student deleted: id='{}'", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/import/template")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<byte[]> downloadTemplate() {
        String csv = "fullName,dateOfBirth,gender,classForAdmission,academicYear,rollNumber,fatherName,fatherMobile,fatherEmail,motherName,motherMobile,motherEmail,address,phone\n"
                + "Rahul Kumar,2012-05-15,Male,Class 7 - A,2025-2026,1,Mr. Rajesh Kumar,9876543210,parent@example.com,Mrs. Sunita Kumar,9876543211,mother@example.com,\"Mumbai, Maharashtra\",9876543210\n";
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=student_import_template.csv")
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping("/import/csv")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<Map<String, Object>> importCsv(@RequestParam("file") MultipartFile file) {
        log.info("[StudentController] POST /api/students/import/csv — file='{}', size={}",
                file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        List<String> errors = new ArrayList<>();
        int imported = 0;
        int total = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "CSV file is empty"));
            }

            String line;
            while ((line = reader.readLine()) != null) {
                total++;
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    String[] cols = parseCsvLine(line);
                    if (cols.length < 4) {
                        errors.add("Row " + total + ": Too few columns (need at least fullName, dateOfBirth, gender, class)");
                        continue;
                    }

                    Student student = new Student();
                    student.setFullName(cols[0].trim());

                    // Parse date (yyyy-MM-dd or dd/MM/yyyy)
                    String dobStr = cols.length > 1 ? cols[1].trim() : "";
                    if (!dobStr.isEmpty()) {
                        try {
                            if (dobStr.contains("/")) {
                                String[] dp = dobStr.split("/");
                                student.setDateOfBirth(LocalDate.of(
                                        Integer.parseInt(dp[2]), Integer.parseInt(dp[1]), Integer.parseInt(dp[0])));
                            } else {
                                student.setDateOfBirth(LocalDate.parse(dobStr));
                            }
                        } catch (Exception e) {
                            errors.add("Row " + total + ": Invalid date '" + dobStr + "'");
                            continue;
                        }
                    }

                    student.setGender(cols.length > 2 ? cols[2].trim() : "");
                    student.setClassForAdmission(cols.length > 3 ? cols[3].trim() : "");
                    student.setAcademicYear(cols.length > 4 ? cols[4].trim() : "");
                    student.setRollNumber(cols.length > 5 ? cols[5].trim() : "");
                    student.setStatus("ACTIVE");

                    // Parent details
                    ParentDetails pd = new ParentDetails();
                    pd.setFatherName(getCol(cols, 6));
                    pd.setFatherMobile(getCol(cols, 7));
                    pd.setFatherEmail(getCol(cols, 8));
                    pd.setMotherName(getCol(cols, 9));
                    pd.setMotherMobile(getCol(cols, 10));
                    pd.setMotherEmail(getCol(cols, 11));
                    if (pd.getFatherName() != null || pd.getMotherName() != null) {
                        student.setParentDetails(pd);
                    }

                    // Contact
                    String address = getCol(cols, 12);
                    String phone = getCol(cols, 13);
                    if (address != null || phone != null) {
                        ContactDetails cd = new ContactDetails();
                        cd.setPermanentAddress(address);
                        cd.setPrimaryContactNumber(phone);
                        student.setContactDetails(cd);
                    }

                    studentService.admitStudent(student);
                    imported++;
                } catch (Exception e) {
                    errors.add("Row " + total + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[StudentController] CSV import failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to read CSV: " + e.getMessage()));
        }

        log.info("[StudentController] CSV import complete: total={}, imported={}, errors={}",
                total, imported, errors.size());
        return ResponseEntity.ok(Map.of(
                "total", total,
                "imported", imported,
                "errors", errors
        ));
    }

    private String getCol(String[] cols, int index) {
        if (index >= cols.length) return null;
        String val = cols[index].trim();
        return val.isEmpty() ? null : val;
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        result.add(field.toString());
        return result.toArray(new String[0]);
    }
}

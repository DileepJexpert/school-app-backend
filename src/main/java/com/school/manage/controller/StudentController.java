package com.school.manage.controller;

import com.school.manage.model.Student;
import com.school.manage.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
}

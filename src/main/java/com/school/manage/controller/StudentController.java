package com.school.manage.controller;

import com.school.manage.model.Student;
import com.school.manage.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // For production, specify allowed origins
public class StudentController {

    private final StudentService studentService;

    /**
     * --- MODIFIED FOR FEE AUTOMATION ---
     * API endpoint to handle a new student admission.
     * This now calls the `admitStudent` service method, which saves the student
     * AND automatically generates their fee profile in a single transaction.
     *
     * @param student The incoming student data from the admission form.
     * @return The newly created student data with a 201 Created status.
     */
    @PostMapping("/add")
    public ResponseEntity<Student> admitNewStudent(@RequestBody Student student) {
        // This is the crucial change: calling the orchestrator method.
        Student admittedStudent = studentService.admitStudent(student);
        return new ResponseEntity<>(admittedStudent, HttpStatus.CREATED);
    }

    /**
     * API endpoint to save an enquiry record.
     * Does NOT generate a fee profile — the student is not yet admitted.
     * Status is forced to ENQUIRY regardless of what was sent.
     *
     * @param student Basic enquiry data (name, contact, class interested).
     * @return The saved enquiry student with a 201 Created status.
     */
    @PostMapping("/enquiry")
    public ResponseEntity<Student> saveEnquiry(@RequestBody Student student) {
        Student saved = studentService.saveEnquiry(student);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * API endpoint to get a list of all students.
     * @return A list of all students with a 200 OK status.
     */
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    /**
     * API endpoint to get a single student by their unique ID.
     *
     * @param id The ID of the student to retrieve.
     * @return The student data if found (200 OK), or a 404 Not Found status.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable String id) {
        return studentService.getStudentById(id)
                .map(ResponseEntity::ok) // If student is found, wrap it in a 200 OK response
                .orElse(ResponseEntity.notFound().build()); // If not found, return a 404
    }

    /**
     * API endpoint to search for students by name.
     *
     * @param name The name to search for.
     * @return A list of matching students.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Student>> searchStudents(@RequestParam String name) {
        List<Student> students = studentService.searchStudents(name);
        return ResponseEntity.ok(students);
    }

    /**
     * API endpoint to update an existing student.
     * This correctly calls the `updateStudent` method, which does NOT re-generate fees.
     *
     * @param id The ID of the student from the URL path.
     * @param student The updated student data from the request body.
     * @return The updated student data.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable String id, @RequestBody Student student) {
        try {
            Student updatedStudent = studentService.updateStudent(id, student);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) { // Catch ResourceNotFoundException or similar
            return ResponseEntity.notFound().build();
        }
    }
}

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
public class StudentController {

    private final StudentService studentService;

    /**
     * API endpoint to handle new student admissions or update existing student data.
     * Your Flutter app will send a POST request with the form data to this URL.
     * This method now correctly calls the `saveStudent` method from the service.
     *
     * @param student The incoming student data, automatically mapped from JSON.
     * @return The newly created or updated student data with a 201 Created status.
     */
    @PostMapping("/add")
    public ResponseEntity<Student> addOrUpdateStudent(@RequestBody Student student) {
        Student savedStudent = studentService.saveStudent(student);
        return new ResponseEntity<>(savedStudent, HttpStatus.CREATED);
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
     * --- NEW ENDPOINT ---
     * API endpoint to get a single student by their unique ID.
     * This is what the StudentDetailPage in your Flutter app will call.
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
     * This now correctly calls the updated search method in the service.
     *
     * @param name The name to search for.
     * @return A list of matching students.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Student>> searchStudents(@RequestParam String name) {
        List<Student> students = studentService.searchStudents(name);
        return ResponseEntity.ok(students);
    }

    // ... inside StudentController.java

    /**
     * API endpoint to update an existing student.
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
package com.school.manage.service;

import com.school.manage.model.Student;
import com.school.manage.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    /**
     * Handles the creation or updating of a student.
     * This method can be used for both new admissions and updating existing records.
     * @param student The student data.
     * @return The saved student object.
     */
    public Student saveStudent(Student student) {
        // You can add more complex logic here if needed, like checking for duplicates
        // or auto-generating admission numbers if they are not provided.
        if (student.getAdmissionNumber() == null || student.getAdmissionNumber().isEmpty()) {
            student.setAdmissionNumber("ADM-" + (studentRepository.count() + 1));
        }
        return studentRepository.save(student);
    }

    /**
     * Retrieves all students from the database.
     * @return A list of all students.
     */
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    /**
     * Finds a single student by their unique ID.
     * @param id The ID of the student.
     * @return An Optional containing the student if found.
     */
    public Optional<Student> getStudentById(String id) {
        return studentRepository.findById(id);
    }

    /**
     * Searches for students by matching a string against their full name.
     * This method now correctly uses the findByFullNameContainingIgnoreCase method
     * from the updated repository.
     *
     * @param name The name to search for.
     * @return A list of students whose full name contains the search string.
     */
    public List<Student> searchStudents(String name) {
        return studentRepository.findByFullNameContainingIgnoreCase(name);
    }
    /**
     * Updates an existing student's details.
     *
     * @param id The ID of the student to update.
     * @param studentDetails The new details for the student.
     * @return The updated student object.
     * @throws ResourceNotFoundException if no student with the given ID is found.
     */
    public Student updateStudent(String id, Student studentDetails) throws Exception {
        // Find the existing student or throw an error if not found
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new Exception("Student not found with id: " + id));

        // Set the ID from the path variable to ensure we update the correct document
        studentDetails.setId(id);

        // Save the student object. Spring Data's save method performs an "upsert":
        // if the ID exists, it updates; if not, it inserts.
        return studentRepository.save(studentDetails);
    }
}
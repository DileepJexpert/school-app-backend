package com.school.manage.service;

import com.school.manage.model.Student;
import com.school.manage.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.util.List;
import java.util.Optional;

@Service
// @RequiredArgsConstructor is great, but we'll switch to @Autowired for clarity with the new service
public class StudentService {

    private final StudentRepository studentRepository;

    // --- NEW: INJECTING THE FEE PROFILE SERVICE ---
    // This creates the connection to your fee management module.
    private final FeeProfileService feeProfileService;

    // --- NEW: CONSTRUCTOR INJECTION ---
    // Using constructor injection is a best practice with Spring.
    @Autowired
    public StudentService(StudentRepository studentRepository, FeeProfileService feeProfileService) {
        this.studentRepository = studentRepository;
        this.feeProfileService = feeProfileService;
    }

    // --- NEW: DEDICATED METHOD FOR NEW ADMISSIONS ---
    /**
     * Orchestrates the entire new student admission process.
     * It saves the student's details and automatically generates their fee profile.
     * @param student The new student data from the admission form.
     * @return The fully admitted student object.
     */
    @Transactional // This ensures the whole process is one atomic operation.
    public Student admitStudent(Student student) {
        // First, save the student to get their unique ID
        Student admittedStudent = saveStudent(student);

        // --- THE AUTOMATIC CONNECTION ---
        // Now, call the fee service to create the fee profile for this new student.
        feeProfileService.createFeeProfileForNewStudent(admittedStudent);

        return admittedStudent;
    }


    /**
     * Saves an enquiry record WITHOUT generating a fee profile.
     * Used when a prospective student walks in to enquire — not yet admitted.
     * @param student The enquiry data (status will be forced to ENQUIRY).
     * @return The saved enquiry student object.
     */
    public Student saveEnquiry(Student student) {
        student.setStatus("ENQUIRY");
        if (student.getAdmissionNumber() == null || student.getAdmissionNumber().isEmpty()) {
            String uniqueNumber = "ENQ-" + (studentRepository.count() + 1001);
            student.setAdmissionNumber(uniqueNumber);
        }
        return studentRepository.save(student);
    }

    // --- YOUR EXISTING FEATURES (UNCHANGED) ---

    /**
     * Handles the creation or updating of a student.
     * This method can be used for both new admissions and updating existing records.
     * @param student The student data.
     * @return The saved student object.
     */
    public Student saveStudent(Student student) {
        if (student.getAdmissionNumber() == null || student.getAdmissionNumber().isEmpty()) {
            // A more robust way to generate a unique admission number
            String uniqueAdmissionNumber = "ADM-" + (studentRepository.count() + 1001);
            student.setAdmissionNumber(uniqueAdmissionNumber);
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
     * @param name The name to search for.
     * @return A list of students whose full name contains the search string.
     */
    public List<Student> searchStudents(String name) {
        return studentRepository.findByFullNameContainingIgnoreCase(name);
    }

    /**
     * Updates an existing student's details.
     * This method does NOT re-generate the fee profile, which is correct.
     * @param id The ID of the student to update.
     * @param studentDetails The new details for the student.
     * @return The updated student object.
     * @throws Exception if no student with the given ID is found.
     */
    public Student updateStudent(String id, Student studentDetails) throws Exception {
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new Exception("Student not found with id: " + id));

        // Ensure you're updating the correct document by setting the ID
        studentDetails.setId(existingStudent.getId());

        return studentRepository.save(studentDetails);
    }

    /**
     * Deletes a student (or enquiry) record by ID.
     * @param id The MongoDB document ID to delete.
     */
    public void deleteStudent(String id) {
        studentRepository.deleteById(id);
    }
}
package com.school.manage.service;

import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.Homework;
import com.school.manage.model.Student;
import com.school.manage.repository.HomeworkRepository;
import com.school.manage.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeworkService {

    private final HomeworkRepository homeworkRepository;
    private final StudentRepository studentRepository;

    public Homework create(Homework homework) {
        if (homework.getAssignedDate() == null) {
            homework.setAssignedDate(LocalDate.now());
        }
        if (homework.getStatus() == null) {
            homework.setStatus("ACTIVE");
        }
        homework.setCreatedAt(LocalDateTime.now());
        log.info("[HomeworkService] Creating homework: '{}' for class {} - {}",
                homework.getTitle(), homework.getClassName(), homework.getSubject());
        return homeworkRepository.save(homework);
    }

    public Homework update(String id, Homework updated) {
        Homework existing = homeworkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found: " + id));
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setClassName(updated.getClassName());
        existing.setSubject(updated.getSubject());
        existing.setDueDate(updated.getDueDate());
        existing.setAcademicYear(updated.getAcademicYear());
        existing.setStatus(updated.getStatus());
        return homeworkRepository.save(existing);
    }

    public void delete(String id) {
        homeworkRepository.deleteById(id);
    }

    public Homework getById(String id) {
        return homeworkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found: " + id));
    }

    public List<Homework> getAll() {
        return homeworkRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Homework> getByClassName(String className) {
        return homeworkRepository.findByClassNameOrderByDueDateDesc(className);
    }

    public List<Homework> getActiveForClass(String className) {
        return homeworkRepository.findByClassNameAndStatusOrderByDueDateDesc(className, "ACTIVE");
    }

    public List<Homework> getByTeacher(String teacherId) {
        return homeworkRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
    }

    /**
     * Get homework for a student by looking up their class from linkedEntityId.
     */
    public List<Homework> getHomeworkForStudent(String studentEntityId) {
        Student student = studentRepository.findById(studentEntityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found: " + studentEntityId));
        String className = student.getClassForAdmission();
        log.info("[HomeworkService] Fetching homework for student {} in class {}",
                studentEntityId, className);
        return homeworkRepository.findByClassNameAndStatusOrderByDueDateDesc(className, "ACTIVE");
    }
}

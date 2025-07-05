package com.school.manage.service;


import com.school.manage.model.StudentResult;
import com.school.manage.repository.StudentResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResultService {
    private final StudentResultRepository studentResultRepository;

    public StudentResult addResult(StudentResult studentResult) {
        return studentResultRepository.save(studentResult);
    }

    public List<StudentResult> getResultsByClassAndYear(String className, int year) {
        return studentResultRepository.findByClassNameAndYear(className, year);
    }

    public List<StudentResult> getResultsForStudent(String studentId) {
        return studentResultRepository.findByStudentId(studentId);
    }
}
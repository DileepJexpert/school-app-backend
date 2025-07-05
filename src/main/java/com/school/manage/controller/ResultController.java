package com.school.manage.controller;


import com.school.manage.model.StudentResult;
import com.school.manage.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class ResultController {
    private final ResultService resultService;

    @PostMapping("/add")
    public StudentResult addResult(@RequestBody StudentResult studentResult) {
        return resultService.addResult(studentResult);
    }

    @GetMapping("/class/{className}/year/{year}")
    public List<StudentResult> getResultsByClassAndYear(@PathVariable String className, @PathVariable int year) {
        return resultService.getResultsByClassAndYear(className, year);
    }

    @GetMapping("/student/{studentId}")
    public List<StudentResult> getResultsForStudent(@PathVariable String studentId) {
        return resultService.getResultsForStudent(studentId);
    }
}
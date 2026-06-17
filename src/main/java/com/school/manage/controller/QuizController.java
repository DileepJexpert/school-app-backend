package com.school.manage.controller;

import com.school.manage.model.Quiz;
import com.school.manage.model.QuizAttempt;
import com.school.manage.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Quiz> createQuiz(@RequestBody Quiz quiz) {
        log.info("[QuizController] POST /api/quizzes — title='{}', class='{}'", quiz.getTitle(), quiz.getClassName());
        return new ResponseEntity<>(quizService.createQuiz(quiz), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        log.info("[QuizController] GET /api/quizzes");
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Quiz> getQuizById(@PathVariable String id) {
        log.info("[QuizController] GET /api/quizzes/{}", id);
        return ResponseEntity.ok(quizService.getQuizById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Quiz> updateQuiz(@PathVariable String id, @RequestBody Quiz quiz) {
        log.info("[QuizController] PUT /api/quizzes/{}", id);
        return ResponseEntity.ok(quizService.updateQuiz(id, quiz));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Quiz> publishQuiz(@PathVariable String id) {
        log.info("[QuizController] PUT /api/quizzes/{}/publish", id);
        return ResponseEntity.ok(quizService.publishQuiz(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Void> deleteQuiz(@PathVariable String id) {
        log.info("[QuizController] DELETE /api/quizzes/{}", id);
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/attempts")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<QuizAttempt>> getQuizAttempts(@PathVariable String id) {
        log.info("[QuizController] GET /api/quizzes/{}/attempts", id);
        return ResponseEntity.ok(quizService.getQuizAttempts(id));
    }
}

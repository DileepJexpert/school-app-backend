package com.school.manage.service;

import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.Quiz;
import com.school.manage.model.QuizAttempt;
import com.school.manage.repository.QuizAttemptRepository;
import com.school.manage.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public Quiz createQuiz(Quiz quiz) {
        quiz.setCreatedAt(LocalDateTime.now());
        if (quiz.getQuestions() != null) {
            for (Quiz.Question question : quiz.getQuestions()) {
                if (question.getId() == null || question.getId().isBlank()) {
                    question.setId(UUID.randomUUID().toString());
                }
                if (question.getMarks() <= 0) {
                    question.setMarks(1);
                }
            }
        }
        log.info("[QuizService] Creating quiz '{}' for class {}", quiz.getTitle(), quiz.getClassName());
        return quizRepository.save(quiz);
    }

    public Quiz updateQuiz(String id, Quiz quiz) {
        Quiz existing = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + id));
        quiz.setId(id);
        quiz.setCreatedAt(existing.getCreatedAt());
        quiz.setCreatedBy(existing.getCreatedBy());
        quiz.setUpdatedAt(LocalDateTime.now());
        if (quiz.getQuestions() != null) {
            for (Quiz.Question question : quiz.getQuestions()) {
                if (question.getId() == null || question.getId().isBlank()) {
                    question.setId(UUID.randomUUID().toString());
                }
                if (question.getMarks() <= 0) {
                    question.setMarks(1);
                }
            }
        }
        log.info("[QuizService] Updating quiz '{}'", id);
        return quizRepository.save(quiz);
    }

    public void deleteQuiz(String id) {
        if (!quizRepository.existsById(id)) {
            throw new ResourceNotFoundException("Quiz not found: " + id);
        }
        quizRepository.deleteById(id);
        log.info("[QuizService] Deleted quiz '{}'", id);
    }

    public Quiz getQuizById(String id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + id));
    }

    public List<Quiz> getQuizzesByClass(String className) {
        return quizRepository.findByClassNameAndPublishedTrueOrderByCreatedAtDesc(className);
    }

    public List<Quiz> getQuizzesByClassAndSubject(String className, String subject) {
        return quizRepository.findByClassNameAndSubjectAndPublishedTrueOrderByCreatedAtDesc(className, subject);
    }

    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAllByOrderByCreatedAtDesc();
    }

    public Quiz publishQuiz(String id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + id));
        quiz.setPublished(true);
        quiz.setUpdatedAt(LocalDateTime.now());
        log.info("[QuizService] Published quiz '{}'", id);
        return quizRepository.save(quiz);
    }

    public QuizAttempt submitAttempt(QuizAttempt attempt, String quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + quizId));

        // Build a map of questionId -> Question for grading
        Map<String, Quiz.Question> questionMap = new HashMap<>();
        if (quiz.getQuestions() != null) {
            for (Quiz.Question q : quiz.getQuestions()) {
                questionMap.put(q.getId(), q);
            }
        }

        int correctAnswers = 0;
        int totalMarks = 0;
        int scoredMarks = 0;

        if (attempt.getAnswers() != null) {
            for (QuizAttempt.Answer answer : attempt.getAnswers()) {
                Quiz.Question question = questionMap.get(answer.getQuestionId());
                if (question != null) {
                    totalMarks += question.getMarks();
                    if (answer.getSelectedOption() >= 0 && answer.getSelectedOption() == question.getCorrectOption()) {
                        answer.setCorrect(true);
                        correctAnswers++;
                        scoredMarks += question.getMarks();
                    } else {
                        answer.setCorrect(false);
                    }
                }
            }
        }

        attempt.setQuizId(quizId);
        attempt.setQuizTitle(quiz.getTitle());
        attempt.setClassName(quiz.getClassName());
        attempt.setSubject(quiz.getSubject());
        attempt.setTotalQuestions(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0);
        attempt.setCorrectAnswers(correctAnswers);
        attempt.setTotalMarks(totalMarks);
        attempt.setScoredMarks(scoredMarks);
        attempt.setPercentage(totalMarks > 0 ? (scoredMarks * 100.0) / totalMarks : 0.0);
        attempt.setCompletedAt(LocalDateTime.now());

        log.info("[QuizService] Student '{}' submitted attempt for quiz '{}' — score: {}/{} ({}%)",
                attempt.getStudentName(), quiz.getTitle(), scoredMarks, totalMarks, String.format("%.1f", attempt.getPercentage()));

        return quizAttemptRepository.save(attempt);
    }

    public List<QuizAttempt> getStudentAttempts(String studentId) {
        return quizAttemptRepository.findByStudentIdOrderByCompletedAtDesc(studentId);
    }

    public Map<String, Object> getStudentProgress(String studentId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentIdOrderByCompletedAtDesc(studentId);
        Map<String, Object> progress = new LinkedHashMap<>();

        progress.put("totalAttempts", attempts.size());

        double averagePercentage = attempts.stream()
                .mapToDouble(QuizAttempt::getPercentage)
                .average()
                .orElse(0.0);
        progress.put("averagePercentage", Math.round(averagePercentage * 100.0) / 100.0);

        // Subject-wise breakdown
        Map<String, List<QuizAttempt>> bySubject = attempts.stream()
                .filter(a -> a.getSubject() != null)
                .collect(Collectors.groupingBy(QuizAttempt::getSubject));

        Map<String, Object> subjectWise = new LinkedHashMap<>();
        for (Map.Entry<String, List<QuizAttempt>> entry : bySubject.entrySet()) {
            Map<String, Object> subjectStats = new LinkedHashMap<>();
            List<QuizAttempt> subjectAttempts = entry.getValue();
            subjectStats.put("attemptCount", subjectAttempts.size());
            double avgScore = subjectAttempts.stream()
                    .mapToDouble(QuizAttempt::getPercentage)
                    .average()
                    .orElse(0.0);
            subjectStats.put("averageScore", Math.round(avgScore * 100.0) / 100.0);
            subjectWise.put(entry.getKey(), subjectStats);
        }
        progress.put("subjectWise", subjectWise);

        // Recent 5 attempts
        List<QuizAttempt> recentAttempts = attempts.stream()
                .limit(5)
                .collect(Collectors.toList());
        progress.put("recentAttempts", recentAttempts);

        return progress;
    }

    public List<QuizAttempt> getQuizAttempts(String quizId) {
        return quizAttemptRepository.findByQuizId(quizId);
    }

    public boolean hasStudentAttempted(String studentId, String quizId) {
        return quizAttemptRepository.findByStudentIdAndQuizId(studentId, quizId).isPresent();
    }

    public Optional<QuizAttempt> getStudentAttemptForQuiz(String studentId, String quizId) {
        return quizAttemptRepository.findByStudentIdAndQuizId(studentId, quizId);
    }
}

package com.school.manage.controller;

import com.school.manage.dto.AttendanceSummaryDto;
import com.school.manage.dto.ChildOverviewDto;
import com.school.manage.model.Attendance;
import com.school.manage.model.BookIssue;
import com.school.manage.model.ExamSchedule;
import com.school.manage.model.Homework;
import com.school.manage.model.Quiz;
import com.school.manage.model.QuizAttempt;
import com.school.manage.model.Student;
import com.school.manage.model.DailyDiary;
import com.school.manage.model.StudyMaterial;
import com.school.manage.model.User;
import com.school.manage.model.TutorialVideo;
import com.school.manage.repository.StudentRepository;
import com.school.manage.service.ExamScheduleService;
import com.school.manage.service.FeeService;
import com.school.manage.service.HomeworkService;
import com.school.manage.service.LibraryService;
import com.school.manage.service.QuizService;
import com.school.manage.service.ReportCardPdfService;
import com.school.manage.service.ResultService;
import com.school.manage.service.StudentPortalService;
import com.school.manage.service.DailyDiaryService;
import com.school.manage.service.StudyMaterialService;
import com.school.manage.service.TutorialVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/student-portal")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentPortalController {

    private final StudentPortalService studentPortalService;
    private final ResultService resultService;
    private final ReportCardPdfService reportCardPdfService;
    private final FeeService feeService;
    private final HomeworkService homeworkService;
    private final TutorialVideoService tutorialVideoService;
    private final LibraryService libraryService;
    private final ExamScheduleService examScheduleService;
    private final StudentRepository studentRepository;
    private final StudyMaterialService studyMaterialService;
    private final QuizService quizService;
    private final DailyDiaryService dailyDiaryService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ChildOverviewDto> getDashboard(Authentication auth) {
        User user = (User) auth.getPrincipal();
        log.info("[StudentPortalController] GET /api/student-portal/dashboard");
        return ResponseEntity.ok(studentPortalService.getDashboard(user.getLinkedEntityId()));
    }

    @GetMapping("/attendance")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Attendance>> getMyAttendance(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(studentPortalService.getMyAttendance(user.getLinkedEntityId(), from, to));
    }

    @GetMapping("/attendance/summary")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AttendanceSummaryDto> getMyAttendanceSummary(
            Authentication auth,
            @RequestParam String academicYear) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(studentPortalService.getMyAttendanceSummary(user.getLinkedEntityId(), academicYear));
    }

    @GetMapping("/results")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMyResults(
            Authentication auth,
            @RequestParam String academicYear) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(resultService.getStudentReportCard(user.getLinkedEntityId(), academicYear));
    }

    @GetMapping("/results/pdf")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<byte[]> downloadMyReportCard(
            Authentication auth,
            @RequestParam String academicYear) {
        User user = (User) auth.getPrincipal();
        byte[] pdf = reportCardPdfService.generateReportCardPdf(user.getLinkedEntityId(), academicYear);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report_card.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/fees")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMyFees(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(feeService.getStudentFeeProfile(user.getLinkedEntityId()));
    }

    @GetMapping("/homework")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Homework>> getMyHomework(Authentication auth) {
        User user = (User) auth.getPrincipal();
        log.info("[StudentPortalController] GET /api/student-portal/homework for user={}", user.getId());
        return ResponseEntity.ok(homeworkService.getHomeworkForStudent(user.getLinkedEntityId()));
    }

    @GetMapping("/videos")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<TutorialVideo>> getMyVideos(Authentication auth) {
        User user = (User) auth.getPrincipal();
        log.info("[StudentPortalController] GET /api/student-portal/videos for user={}", user.getId());
        return ResponseEntity.ok(tutorialVideoService.getVideosForStudent(user.getLinkedEntityId()));
    }

    @GetMapping("/library/my-books")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<BookIssue>> getMyBooks(Authentication auth) {
        User user = (User) auth.getPrincipal();
        log.info("[StudentPortalController] GET /api/student-portal/library/my-books for user={}", user.getId());
        return ResponseEntity.ok(libraryService.getStudentIssues(user.getLinkedEntityId()));
    }

    @GetMapping("/exam-schedule")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ExamSchedule>> getMyExamSchedule(
            Authentication auth,
            @RequestParam(required = false) String academicYear) {
        User user = (User) auth.getPrincipal();
        log.info("[StudentPortalController] GET /api/student-portal/exam-schedule for user={}", user.getId());
        Student student = studentRepository.findById(user.getLinkedEntityId())
                .orElseThrow(() -> new RuntimeException("Student not found: " + user.getLinkedEntityId()));
        String year = (academicYear != null && !academicYear.isBlank()) ? academicYear : student.getAcademicYear();
        return ResponseEntity.ok(examScheduleService.getPublishedSchedules(student.getClassForAdmission(), year));
    }

    // ─── Study Materials ────────────────────────────────────────────────

    @GetMapping("/study-materials")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<StudyMaterial>> getMyStudyMaterials(
            Authentication auth,
            @RequestParam(required = false) String subject) {
        User user = (User) auth.getPrincipal();
        log.info("[StudentPortalController] GET /api/student-portal/study-materials for user={}", user.getId());
        Student student = studentRepository.findById(user.getLinkedEntityId())
                .orElseThrow(() -> new RuntimeException("Student not found: " + user.getLinkedEntityId()));
        String className = student.getClassForAdmission();
        if (subject != null && !subject.isBlank()) {
            return ResponseEntity.ok(studyMaterialService.getMaterialsByClassAndSubject(className, subject));
        }
        return ResponseEntity.ok(studyMaterialService.getMaterialsByClass(className));
    }

    // ─── Practice Quizzes ───────────────────────────────────────────────

    @GetMapping("/quizzes")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Quiz>> getMyQuizzes(
            Authentication auth,
            @RequestParam(required = false) String subject) {
        User user = (User) auth.getPrincipal();
        log.info("[StudentPortalController] GET /api/student-portal/quizzes for user={}", user.getId());
        Student student = studentRepository.findById(user.getLinkedEntityId())
                .orElseThrow(() -> new RuntimeException("Student not found: " + user.getLinkedEntityId()));
        String className = student.getClassForAdmission();
        List<Quiz> quizzes;
        if (subject != null && !subject.isBlank()) {
            quizzes = quizService.getQuizzesByClassAndSubject(className, subject);
        } else {
            quizzes = quizService.getQuizzesByClass(className);
        }
        // Strip correct answers before returning to students
        return ResponseEntity.ok(stripAnswers(quizzes));
    }

    @PostMapping("/quizzes/{quizId}/attempt")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<QuizAttempt> submitQuizAttempt(
            Authentication auth,
            @PathVariable String quizId,
            @RequestBody QuizAttempt attempt) {
        User user = (User) auth.getPrincipal();
        log.info("[StudentPortalController] POST /api/student-portal/quizzes/{}/attempt for user={}", quizId, user.getId());
        Student student = studentRepository.findById(user.getLinkedEntityId())
                .orElseThrow(() -> new RuntimeException("Student not found: " + user.getLinkedEntityId()));
        attempt.setStudentId(user.getLinkedEntityId());
        attempt.setStudentName(student.getFullName());
        return ResponseEntity.ok(quizService.submitAttempt(attempt, quizId));
    }

    @GetMapping("/quiz-attempts")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<QuizAttempt>> getMyAttempts(Authentication auth) {
        User user = (User) auth.getPrincipal();
        log.info("[StudentPortalController] GET /api/student-portal/quiz-attempts for user={}", user.getId());
        return ResponseEntity.ok(quizService.getStudentAttempts(user.getLinkedEntityId()));
    }

    @GetMapping("/quiz-progress")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getMyProgress(Authentication auth) {
        User user = (User) auth.getPrincipal();
        log.info("[StudentPortalController] GET /api/student-portal/quiz-progress for user={}", user.getId());
        return ResponseEntity.ok(quizService.getStudentProgress(user.getLinkedEntityId()));
    }

    @GetMapping("/quizzes/{quizId}/attempted")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> hasAttempted(
            Authentication auth,
            @PathVariable String quizId) {
        User user = (User) auth.getPrincipal();
        log.info("[StudentPortalController] GET /api/student-portal/quizzes/{}/attempted for user={}", quizId, user.getId());
        Map<String, Object> result = new LinkedHashMap<>();
        Optional<QuizAttempt> existingAttempt = quizService.getStudentAttemptForQuiz(user.getLinkedEntityId(), quizId);
        result.put("attempted", existingAttempt.isPresent());
        existingAttempt.ifPresent(a -> result.put("attempt", a));
        return ResponseEntity.ok(result);
    }

    // ─── Daily Diary ─────────────────────────────────────────────────────

    @GetMapping("/daily-diary")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<DailyDiary>> getMyDailyDiary(
            Authentication auth,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        User user = (User) auth.getPrincipal();
        log.info("[StudentPortalController] GET /api/student-portal/daily-diary for user={}", user.getId());
        Student student = studentRepository.findById(user.getLinkedEntityId())
                .orElseThrow(() -> new RuntimeException("Student not found: " + user.getLinkedEntityId()));
        String className = student.getClassForAdmission();
        if (date != null) {
            return ResponseEntity.ok(dailyDiaryService.getByClassAndDate(className, date));
        }
        return ResponseEntity.ok(dailyDiaryService.getByClass(className));
    }

    @GetMapping("/daily-diary/today")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<DailyDiary>> getMyDailyDiaryToday(Authentication auth) {
        User user = (User) auth.getPrincipal();
        log.info("[StudentPortalController] GET /api/student-portal/daily-diary/today for user={}", user.getId());
        Student student = studentRepository.findById(user.getLinkedEntityId())
                .orElseThrow(() -> new RuntimeException("Student not found: " + user.getLinkedEntityId()));
        String className = student.getClassForAdmission();
        return ResponseEntity.ok(dailyDiaryService.getByClassAndDate(className, LocalDate.now()));
    }

    // ─── Helper: strip correct answers from quizzes for student view ────

    private List<Quiz> stripAnswers(List<Quiz> quizzes) {
        List<Quiz> stripped = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            Quiz copy = new Quiz();
            copy.setId(quiz.getId());
            copy.setTitle(quiz.getTitle());
            copy.setDescription(quiz.getDescription());
            copy.setSubject(quiz.getSubject());
            copy.setClassName(quiz.getClassName());
            copy.setChapter(quiz.getChapter());
            copy.setAcademicYear(quiz.getAcademicYear());
            copy.setTimeLimit(quiz.getTimeLimit());
            copy.setDifficulty(quiz.getDifficulty());
            copy.setPublished(quiz.isPublished());
            copy.setShuffleQuestions(quiz.isShuffleQuestions());
            copy.setCreatedBy(quiz.getCreatedBy());
            copy.setCreatedAt(quiz.getCreatedAt());
            copy.setUpdatedAt(quiz.getUpdatedAt());
            if (quiz.getQuestions() != null) {
                List<Quiz.Question> strippedQuestions = new ArrayList<>();
                for (Quiz.Question q : quiz.getQuestions()) {
                    Quiz.Question qCopy = new Quiz.Question();
                    qCopy.setId(q.getId());
                    qCopy.setQuestionText(q.getQuestionText());
                    qCopy.setOptions(q.getOptions());
                    qCopy.setCorrectOption(-1); // hide correct answer
                    qCopy.setExplanation(null);  // hide explanation
                    qCopy.setMarks(q.getMarks());
                    strippedQuestions.add(qCopy);
                }
                copy.setQuestions(strippedQuestions);
            }
            stripped.add(copy);
        }
        return stripped;
    }
}

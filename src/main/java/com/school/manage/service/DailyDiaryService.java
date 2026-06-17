package com.school.manage.service;

import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.DailyDiary;
import com.school.manage.repository.DailyDiaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyDiaryService {

    private final DailyDiaryRepository dailyDiaryRepository;

    public DailyDiary create(DailyDiary diary) {
        diary.setCreatedAt(LocalDateTime.now());
        log.info("[DailyDiaryService] Creating diary entry: '{}' for class {} - {}",
                diary.getTitle(), diary.getClassName(), diary.getSubject());
        return dailyDiaryRepository.save(diary);
    }

    public DailyDiary update(String id, DailyDiary updated) {
        DailyDiary existing = dailyDiaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Daily diary entry not found: " + id));
        existing.setClassName(updated.getClassName());
        existing.setSection(updated.getSection());
        existing.setSubject(updated.getSubject());
        existing.setDate(updated.getDate());
        existing.setTitle(updated.getTitle());
        existing.setContent(updated.getContent());
        existing.setHomework(updated.getHomework());
        existing.setAcademicYear(updated.getAcademicYear());
        existing.setUpdatedAt(LocalDateTime.now());
        return dailyDiaryRepository.save(existing);
    }

    public void delete(String id) {
        dailyDiaryRepository.deleteById(id);
    }

    public DailyDiary getById(String id) {
        return dailyDiaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Daily diary entry not found: " + id));
    }

    public List<DailyDiary> getAll() {
        return dailyDiaryRepository.findAll();
    }

    public List<DailyDiary> getByClass(String className) {
        return dailyDiaryRepository.findByClassName(className);
    }

    public List<DailyDiary> getByClassAndDate(String className, LocalDate date) {
        return dailyDiaryRepository.findByClassNameAndDate(className, date);
    }

    public List<DailyDiary> getByClassAndSubject(String className, String subject) {
        return dailyDiaryRepository.findByClassNameAndSubject(className, subject);
    }

    public List<DailyDiary> getByTeacher(String teacherId) {
        return dailyDiaryRepository.findByTeacherId(teacherId);
    }

    public List<DailyDiary> getByClassAndDateRange(String className, LocalDate from, LocalDate to) {
        return dailyDiaryRepository.findByClassNameAndDateBetween(className, from, to);
    }
}

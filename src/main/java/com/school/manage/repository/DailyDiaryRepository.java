package com.school.manage.repository;

import com.school.manage.model.DailyDiary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyDiaryRepository extends MongoRepository<DailyDiary, String> {

    List<DailyDiary> findByClassName(String className);

    List<DailyDiary> findByClassNameAndDate(String className, LocalDate date);

    List<DailyDiary> findByClassNameAndSubject(String className, String subject);

    List<DailyDiary> findByTeacherId(String teacherId);

    List<DailyDiary> findByClassNameAndDateBetween(String className, LocalDate from, LocalDate to);
}

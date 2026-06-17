package com.school.manage.repository;

import com.school.manage.model.Meeting;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface MeetingRepository extends MongoRepository<Meeting, String> {

    List<Meeting> findByTeacherId(String teacherId);

    List<Meeting> findByParentId(String parentId);

    List<Meeting> findByStudentId(String studentId);

    List<Meeting> findByDate(LocalDate date);

    List<Meeting> findByTeacherIdAndDate(String teacherId, LocalDate date);

    List<Meeting> findByDateBetween(LocalDate from, LocalDate to);

    List<Meeting> findByStatus(String status);

    List<Meeting> findByClassName(String className);

    List<Meeting> findAllByOrderByDateDescStartTimeDesc();
}

package com.school.manage.repository;

import com.school.manage.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends MongoRepository<Event, String> {
    List<Event> findByActiveTrueOrderByStartDateDesc();
    List<Event> findByStartDateBetweenAndActiveTrue(LocalDate from, LocalDate to);
    List<Event> findByCategoryAndActiveTrue(String category);
    List<Event> findByIsHolidayTrueAndActiveTrueOrderByStartDateAsc();
    List<Event> findByStartDateGreaterThanEqualAndActiveTrueOrderByStartDateAsc(LocalDate from);
}

package com.school.manage.service;

import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.Timetable;
import com.school.manage.repository.TimetableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableRepository timetableRepository;

    /**
     * Creates or updates a timetable entry for a specific class, academic year and day.
     * If a timetable for the same class/year/day exists, it is replaced.
     */
    public Timetable saveOrUpdateTimetable(Timetable timetable) {
        timetableRepository
                .findByClassNameAndAcademicYearAndDayOfWeek(
                        timetable.getClassName(),
                        timetable.getAcademicYear(),
                        timetable.getDayOfWeek())
                .ifPresent(existing -> timetable.setId(existing.getId()));

        return timetableRepository.save(timetable);
    }

    /**
     * Returns the full weekly timetable for a class in a given academic year.
     */
    public List<Timetable> getTimetableByClass(String className, String academicYear) {
        List<Timetable> result = timetableRepository
                .findByClassNameAndAcademicYear(className, academicYear);
        if (result.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No timetable found for class: " + className + " year: " + academicYear);
        }
        return result;
    }

    /**
     * Returns the timetable for a class on a specific day.
     */
    public Timetable getTimetableByClassAndDay(
            String className, String academicYear, String dayOfWeek) {
        return timetableRepository
                .findByClassNameAndAcademicYearAndDayOfWeek(className, academicYear, dayOfWeek)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No timetable found for class: " + className
                                + " on day: " + dayOfWeek));
    }

    /**
     * Returns a timetable entry by its ID.
     */
    public Timetable getTimetableById(String id) {
        return timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Timetable not found with id: " + id));
    }

    /**
     * Deletes all timetable entries for a class in a given academic year.
     */
    public void deleteTimetableByClass(String className, String academicYear) {
        timetableRepository.deleteByClassNameAndAcademicYear(className, academicYear);
    }

    /**
     * Deletes a timetable entry by its ID.
     */
    public void deleteTimetableById(String id) {
        if (!timetableRepository.existsById(id)) {
            throw new ResourceNotFoundException("Timetable not found with id: " + id);
        }
        timetableRepository.deleteById(id);
    }
}

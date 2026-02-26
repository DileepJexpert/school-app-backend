package com.school.manage.controller;

import com.school.manage.model.Timetable;
import com.school.manage.service.TimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TimetableController {

    private final TimetableService timetableService;

    /**
     * Create or update a timetable for a specific class, year and day.
     *
     * POST /api/timetable
     */
    @PostMapping
    public ResponseEntity<Timetable> saveOrUpdateTimetable(@RequestBody Timetable timetable) {
        return new ResponseEntity<>(
                timetableService.saveOrUpdateTimetable(timetable), HttpStatus.CREATED);
    }

    /**
     * Get the full weekly timetable for a class.
     *
     * GET /api/timetable/{className}?academicYear=2024-25
     */
    @GetMapping("/{className}")
    public ResponseEntity<List<Timetable>> getTimetableByClass(
            @PathVariable String className,
            @RequestParam String academicYear) {
        return ResponseEntity.ok(
                timetableService.getTimetableByClass(className, academicYear));
    }

    /**
     * Get the timetable for a class on a specific day.
     *
     * GET /api/timetable/{className}/{dayOfWeek}?academicYear=2024-25
     */
    @GetMapping("/{className}/{dayOfWeek}")
    public ResponseEntity<Timetable> getTimetableByClassAndDay(
            @PathVariable String className,
            @PathVariable String dayOfWeek,
            @RequestParam String academicYear) {
        return ResponseEntity.ok(
                timetableService.getTimetableByClassAndDay(className, academicYear, dayOfWeek));
    }

    /**
     * Get a single timetable entry by ID.
     *
     * GET /api/timetable/entry/{id}
     */
    @GetMapping("/entry/{id}")
    public ResponseEntity<Timetable> getTimetableById(@PathVariable String id) {
        return ResponseEntity.ok(timetableService.getTimetableById(id));
    }

    /**
     * Delete all timetable entries for a class in an academic year.
     *
     * DELETE /api/timetable/{className}?academicYear=2024-25
     */
    @DeleteMapping("/{className}")
    public ResponseEntity<Void> deleteTimetableByClass(
            @PathVariable String className,
            @RequestParam String academicYear) {
        timetableService.deleteTimetableByClass(className, academicYear);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a single timetable entry by ID.
     *
     * DELETE /api/timetable/entry/{id}
     */
    @DeleteMapping("/entry/{id}")
    public ResponseEntity<Void> deleteTimetableById(@PathVariable String id) {
        timetableService.deleteTimetableById(id);
        return ResponseEntity.noContent().build();
    }
}

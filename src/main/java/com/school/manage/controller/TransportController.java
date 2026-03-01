package com.school.manage.controller;

import com.school.manage.model.Bus;
import com.school.manage.model.StudentTransportAssignment;
import com.school.manage.model.TransportRoute;
import com.school.manage.service.TransportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transport")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransportController {

    private final TransportService transportService;

    // ── Buses ─────────────────────────────────────────────────────────────────

    /** GET /api/transport/buses */
    @GetMapping("/buses")
    public ResponseEntity<List<Bus>> getAllBuses() {
        return ResponseEntity.ok(transportService.getAllBuses());
    }

    /** POST /api/transport/buses */
    @PostMapping("/buses")
    public ResponseEntity<Bus> createBus(@RequestBody Bus bus) {
        return new ResponseEntity<>(transportService.createBus(bus), HttpStatus.CREATED);
    }

    /** PUT /api/transport/buses/{id} */
    @PutMapping("/buses/{id}")
    public ResponseEntity<Bus> updateBus(
            @PathVariable String id, @RequestBody Bus bus) {
        return ResponseEntity.ok(transportService.updateBus(id, bus));
    }

    /** DELETE /api/transport/buses/{id} */
    @DeleteMapping("/buses/{id}")
    public ResponseEntity<Void> deleteBus(@PathVariable String id) {
        transportService.deleteBus(id);
        return ResponseEntity.noContent().build();
    }

    // ── Routes ────────────────────────────────────────────────────────────────

    /** GET /api/transport/routes */
    @GetMapping("/routes")
    public ResponseEntity<List<TransportRoute>> getAllRoutes() {
        return ResponseEntity.ok(transportService.getAllRoutes());
    }

    /** POST /api/transport/routes */
    @PostMapping("/routes")
    public ResponseEntity<TransportRoute> createRoute(@RequestBody TransportRoute route) {
        return new ResponseEntity<>(transportService.createRoute(route), HttpStatus.CREATED);
    }

    /** PUT /api/transport/routes/{id} */
    @PutMapping("/routes/{id}")
    public ResponseEntity<TransportRoute> updateRoute(
            @PathVariable String id, @RequestBody TransportRoute route) {
        return ResponseEntity.ok(transportService.updateRoute(id, route));
    }

    /** DELETE /api/transport/routes/{id} */
    @DeleteMapping("/routes/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable String id) {
        transportService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    // ── Assignments ───────────────────────────────────────────────────────────

    /** GET /api/transport/assignments — all active assignments */
    @GetMapping("/assignments")
    public ResponseEntity<List<StudentTransportAssignment>> getAllAssignments() {
        return ResponseEntity.ok(transportService.getAllActiveAssignments());
    }

    /** GET /api/transport/assignments/bus/{busId} — roster for one bus */
    @GetMapping("/assignments/bus/{busId}")
    public ResponseEntity<List<StudentTransportAssignment>> getByBus(
            @PathVariable String busId) {
        return ResponseEntity.ok(transportService.getAssignmentsByBus(busId));
    }

    /**
     * GET /api/transport/assignments/student/{studentId}
     * Returns 200 with the assignment, or 204 if student has no active assignment.
     */
    @GetMapping("/assignments/student/{studentId}")
    public ResponseEntity<StudentTransportAssignment> getByStudent(
            @PathVariable String studentId) {
        return transportService.getAssignmentByStudent(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /** POST /api/transport/assignments — assign (or re-assign) a student */
    @PostMapping("/assignments")
    public ResponseEntity<StudentTransportAssignment> assignStudent(
            @RequestBody StudentTransportAssignment assignment) {
        return new ResponseEntity<>(
                transportService.assignStudent(assignment), HttpStatus.CREATED);
    }

    /** DELETE /api/transport/assignments/{id} — soft-delete (sets INACTIVE) */
    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<Void> removeAssignment(@PathVariable String id) {
        transportService.removeAssignment(id);
        return ResponseEntity.noContent().build();
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    /** GET /api/transport/stats */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(transportService.getStats());
    }
}

package com.school.manage.controller;

import com.school.manage.model.Bus;
import com.school.manage.model.StudentTransportAssignment;
import com.school.manage.model.TransportRoute;
import com.school.manage.service.TransportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transport")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransportController {

    private final TransportService transportService;

    @GetMapping("/buses")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER','STUDENT','PARENT')")
    public ResponseEntity<List<Bus>> getAllBuses() {
        return ResponseEntity.ok(transportService.getAllBuses());
    }

    @PostMapping("/buses")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<Bus> createBus(@RequestBody Bus bus) {
        return new ResponseEntity<>(transportService.createBus(bus), HttpStatus.CREATED);
    }

    @PutMapping("/buses/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<Bus> updateBus(
            @PathVariable String id, @RequestBody Bus bus) {
        return ResponseEntity.ok(transportService.updateBus(id, bus));
    }

    @DeleteMapping("/buses/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<Void> deleteBus(@PathVariable String id) {
        transportService.deleteBus(id);
        return ResponseEntity.noContent().build();
    }

    // ── Routes ────────────────────────────────────────────────────────────────

    @GetMapping("/routes")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER','STUDENT','PARENT')")
    public ResponseEntity<List<TransportRoute>> getAllRoutes() {
        return ResponseEntity.ok(transportService.getAllRoutes());
    }

    @PostMapping("/routes")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<TransportRoute> createRoute(@RequestBody TransportRoute route) {
        return new ResponseEntity<>(transportService.createRoute(route), HttpStatus.CREATED);
    }

    @PutMapping("/routes/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<TransportRoute> updateRoute(
            @PathVariable String id, @RequestBody TransportRoute route) {
        return ResponseEntity.ok(transportService.updateRoute(id, route));
    }

    @DeleteMapping("/routes/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<Void> deleteRoute(@PathVariable String id) {
        transportService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    // ── Assignments ───────────────────────────────────────────────────────────

    @GetMapping("/assignments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<List<StudentTransportAssignment>> getAllAssignments() {
        return ResponseEntity.ok(transportService.getAllActiveAssignments());
    }

    @GetMapping("/assignments/bus/{busId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<List<StudentTransportAssignment>> getByBus(
            @PathVariable String busId) {
        return ResponseEntity.ok(transportService.getAssignmentsByBus(busId));
    }

    @GetMapping("/assignments/student/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER','STUDENT','PARENT')")
    public ResponseEntity<StudentTransportAssignment> getByStudent(
            @PathVariable String studentId) {
        return transportService.getAssignmentByStudent(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/assignments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<StudentTransportAssignment> assignStudent(
            @RequestBody StudentTransportAssignment assignment) {
        return new ResponseEntity<>(
                transportService.assignStudent(assignment), HttpStatus.CREATED);
    }

    @DeleteMapping("/assignments/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<Void> removeAssignment(@PathVariable String id) {
        transportService.removeAssignment(id);
        return ResponseEntity.noContent().build();
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(transportService.getStats());
    }
}

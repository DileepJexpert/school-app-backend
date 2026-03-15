package com.school.manage.controller;

import com.school.manage.model.Bus;
import com.school.manage.model.StudentTransportAssignment;
import com.school.manage.model.TransportRoute;
import com.school.manage.service.TransportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/transport")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransportController {

    private final TransportService transportService;

    @GetMapping("/buses")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER','STUDENT','PARENT')")
    public ResponseEntity<List<Bus>> getAllBuses() {
        log.debug("[TransportController] GET /api/transport/buses");
        return ResponseEntity.ok(transportService.getAllBuses());
    }

    @PostMapping("/buses")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<Bus> createBus(@RequestBody Bus bus) {
        log.info("[TransportController] POST /api/transport/buses — busNumber='{}'", bus.getBusNumber());
        Bus saved = transportService.createBus(bus);
        log.info("[TransportController] Bus created: id='{}'", saved.getId());
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/buses/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<Bus> updateBus(@PathVariable String id, @RequestBody Bus bus) {
        log.info("[TransportController] PUT /api/transport/buses/{}", id);
        return ResponseEntity.ok(transportService.updateBus(id, bus));
    }

    @DeleteMapping("/buses/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<Void> deleteBus(@PathVariable String id) {
        log.info("[TransportController] DELETE /api/transport/buses/{}", id);
        transportService.deleteBus(id);
        return ResponseEntity.noContent().build();
    }

    // ── Routes ────────────────────────────────────────────────────────────────

    @GetMapping("/routes")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER','STUDENT','PARENT')")
    public ResponseEntity<List<TransportRoute>> getAllRoutes() {
        log.debug("[TransportController] GET /api/transport/routes");
        return ResponseEntity.ok(transportService.getAllRoutes());
    }

    @PostMapping("/routes")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<TransportRoute> createRoute(@RequestBody TransportRoute route) {
        log.info("[TransportController] POST /api/transport/routes — zoneName='{}'", route.getZoneName());
        TransportRoute saved = transportService.createRoute(route);
        log.info("[TransportController] Route created: id='{}'", saved.getId());
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/routes/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<TransportRoute> updateRoute(@PathVariable String id, @RequestBody TransportRoute route) {
        log.info("[TransportController] PUT /api/transport/routes/{}", id);
        return ResponseEntity.ok(transportService.updateRoute(id, route));
    }

    @DeleteMapping("/routes/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<Void> deleteRoute(@PathVariable String id) {
        log.info("[TransportController] DELETE /api/transport/routes/{}", id);
        transportService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    // ── Assignments ───────────────────────────────────────────────────────────

    @GetMapping("/assignments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<List<StudentTransportAssignment>> getAllAssignments() {
        log.debug("[TransportController] GET /api/transport/assignments");
        return ResponseEntity.ok(transportService.getAllActiveAssignments());
    }

    @GetMapping("/assignments/bus/{busId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<List<StudentTransportAssignment>> getByBus(@PathVariable String busId) {
        log.debug("[TransportController] GET /api/transport/assignments/bus/{}", busId);
        return ResponseEntity.ok(transportService.getAssignmentsByBus(busId));
    }

    @GetMapping("/assignments/student/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER','STUDENT','PARENT')")
    public ResponseEntity<StudentTransportAssignment> getByStudent(@PathVariable String studentId) {
        log.debug("[TransportController] GET /api/transport/assignments/student/{}", studentId);
        return transportService.getAssignmentByStudent(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/assignments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<StudentTransportAssignment> assignStudent(
            @RequestBody StudentTransportAssignment assignment) {
        log.info("[TransportController] POST /api/transport/assignments — studentId='{}', busId='{}'",
                assignment.getStudentId(), assignment.getBusId());
        StudentTransportAssignment saved = transportService.assignStudent(assignment);
        log.info("[TransportController] Transport assignment created: id='{}'", saved.getId());
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @DeleteMapping("/assignments/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<Void> removeAssignment(@PathVariable String id) {
        log.info("[TransportController] DELETE /api/transport/assignments/{}", id);
        transportService.removeAssignment(id);
        return ResponseEntity.noContent().build();
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TRANSPORT_MANAGER')")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.debug("[TransportController] GET /api/transport/stats");
        return ResponseEntity.ok(transportService.getStats());
    }
}
